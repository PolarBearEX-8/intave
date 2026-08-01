/*
 * Copyright 2026 Intave
 *
 * This software is licensed under the PolyForm Perimeter License 1.0.0.
 * You may use this software for any purpose, except for providing to
 * others any product that competes with the software.
 *
 * A copy of the license is available at:
 *   https://polyformproject.org/licenses/perimeter/1.0.0/
 */

package de.jpx3.intave.cloud.protocol.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.ScheduledFuture;

import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Coalesces encoded packets so that they share a compression frame.
 */
public final class Batching extends ChannelDuplexHandler {
	private final int maximumBatchSize;
	private final long maximumDelay;
	private final TimeUnit delayUnit;

	private ByteBuf batch;
	private List<ChannelPromise> batchPromises;
	private ScheduledFuture<?> scheduledFlush;
	private boolean pendingDownstreamWrites;

	public Batching(int maximumBatchSize, long maximumDelay, TimeUnit delayUnit) {
		if (maximumBatchSize <= 0 || maximumDelay < 0) {
			throw new IllegalArgumentException();
		}
		this.maximumBatchSize = maximumBatchSize;
		this.maximumDelay = maximumDelay;
		this.delayUnit = Objects.requireNonNull(delayUnit);
	}

	@Override
	public void write(ChannelHandlerContext context, Object message, ChannelPromise promise) {
		if (!(message instanceof ByteBuf)) {
			flushNow(context);
			context.write(message, promise);
			pendingDownstreamWrites = true;
			return;
		}

		ByteBuf packet = (ByteBuf) message;
		int packetSize = packet.readableBytes();
		if (packetSize >= maximumBatchSize) {
			flushNow(context);
			context.write(packet, promise);
			pendingDownstreamWrites = true;
			return;
		}

		if (batch != null && batch.readableBytes() + packetSize > maximumBatchSize) {
			flushNow(context);
		}
		if (batch == null) {
			batch = context.alloc().buffer(Math.min(maximumBatchSize, Math.max(256, packetSize)));
			batchPromises = new ArrayList<>();
		}
		batch.writeBytes(packet, packet.readerIndex(), packetSize);
		batchPromises.add(promise);
		ReferenceCountUtil.release(packet);

		if (batch.readableBytes() >= maximumBatchSize) {
			flushNow(context);
		}
	}

	@Override
	public void flush(ChannelHandlerContext context) {
		if (batch == null && !pendingDownstreamWrites) {
			context.flush();
			return;
		}
		if (scheduledFlush == null) {
			scheduledFlush = context.executor().schedule(
				() -> flushNow(context), maximumDelay, delayUnit
			);
		}
	}

	@Override
	public void close(ChannelHandlerContext context, ChannelPromise promise) {
		flushNow(context);
		context.close(promise);
	}

	@Override
	public void disconnect(ChannelHandlerContext context, ChannelPromise promise) {
		flushNow(context);
		context.disconnect(promise);
	}

	@Override
	public void channelInactive(ChannelHandlerContext context) {
		cancelScheduledFlush();
		failBatch(new ClosedChannelException());
		context.fireChannelInactive();
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext context) {
		cancelScheduledFlush();
		failBatch(new ClosedChannelException());
	}

	private void flushNow(ChannelHandlerContext context) {
		cancelScheduledFlush();
		emitBatch(context);
		if (pendingDownstreamWrites) {
			context.flush();
			pendingDownstreamWrites = false;
		}
	}

	private void emitBatch(ChannelHandlerContext context) {
		if (batch == null) {
			return;
		}

		ByteBuf completedBatch = batch;
		List<ChannelPromise> completedPromises = batchPromises;
		batch = null;
		batchPromises = null;
		pendingDownstreamWrites = true;

		ChannelPromise combinedPromise = context.newPromise();
		combinedPromise.addListener(future -> completePromises(
			completedPromises, future.isSuccess(), future.cause()
		));
		try {
			context.write(completedBatch, combinedPromise);
		} catch (Throwable throwable) {
			ReferenceCountUtil.release(completedBatch);
			combinedPromise.tryFailure(throwable);
		}
	}

	private void cancelScheduledFlush() {
		if (scheduledFlush != null) {
			scheduledFlush.cancel(false);
			scheduledFlush = null;
		}
	}

	private void failBatch(Throwable throwable) {
		if (batch == null) {
			return;
		}
		ReferenceCountUtil.release(batch);
		batch = null;
		List<ChannelPromise> promises = batchPromises;
		batchPromises = null;
		for (ChannelPromise promise : promises) {
			promise.tryFailure(throwable);
		}
	}

	private static void completePromises(
		List<ChannelPromise> promises, boolean success, Throwable failure
	) {
		Throwable actualFailure = failure == null ? new ClosedChannelException() : failure;
		for (ChannelPromise promise : promises) {
			if (success) {
				promise.trySuccess();
			} else {
				promise.tryFailure(actualFailure);
			}
		}
	}
}
