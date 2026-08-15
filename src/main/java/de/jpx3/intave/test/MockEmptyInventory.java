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

package de.jpx3.intave.test;

import com.google.common.collect.Maps;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import static java.lang.Math.min;

public final class MockEmptyInventory implements PlayerInventory {
	private final ItemStack[] storage = new ItemStack[36];
	private final ItemStack[] armor = new ItemStack[4];
	private final ItemStack[] extra = new ItemStack[4];
	private int heldItemSlot;

  @Override
  public ItemStack[] getArmorContents() {
		return armor.clone();
  }

  @Override
  public ItemStack[] getExtraContents() {
		return extra.clone();
  }

  @Override
  public ItemStack getHelmet() {
		return armor[3];
  }

  @Override
  public ItemStack getChestplate() {
		return armor[2];
  }

  @Override
  public ItemStack getLeggings() {
		return armor[1];
  }

  @Override
  public ItemStack getBoots() {
		return armor[0];
  }

  @Override
  public int getSize() {
    return 36;
  }

  @Override
  public int getMaxStackSize() {
    return 64;
  }

  @Override
  public void setMaxStackSize(int i) {

  }

  @Override
  public String getName() {
    return "MockEmptyInventory";
  }

  @Override
  public ItemStack getItem(int i) {
		return i < 0 || i >= storage.length ? null : storage[i];
  }

  @Override
  public void setItem(int i, ItemStack itemStack) {
		if (i >= 0 && i < storage.length) {
			storage[i] = itemStack;
		}
  }

  @Override
  public HashMap<Integer, ItemStack> addItem(ItemStack... itemStacks) throws IllegalArgumentException {
    return Maps.newHashMap();
  }

  @Override
  public HashMap<Integer, ItemStack> removeItem(ItemStack... itemStacks) throws IllegalArgumentException {
    return Maps.newHashMap();
  }

  @Override
  public ItemStack[] getContents() {
		ItemStack[] contents = new ItemStack[storage.length + armor.length + extra.length];
		System.arraycopy(storage, 0, contents, 0, storage.length);
		System.arraycopy(armor, 0, contents, storage.length, armor.length);
		System.arraycopy(extra, 0, contents, storage.length + armor.length, extra.length);
		return contents;
  }

  @Override
  public void setContents(ItemStack[] itemStacks) throws IllegalArgumentException {
		clear();
		System.arraycopy(itemStacks, 0, storage, 0, min(storage.length, itemStacks.length));
  }

  @Override
  public ItemStack[] getStorageContents() {
		return storage.clone();
  }

  @Override
  public void setStorageContents(ItemStack[] itemStacks) throws IllegalArgumentException {
		java.util.Arrays.fill(storage, null);
		System.arraycopy(itemStacks, 0, storage, 0, min(storage.length, itemStacks.length));
  }

  @Override
  public boolean contains(int i) {
    return false;
  }

  @Override
  public boolean contains(Material material) throws IllegalArgumentException {
    return false;
  }

  @Override
  public boolean contains(ItemStack itemStack) {
    return false;
  }

  @Override
  public boolean contains(int i, int i1) {
    return false;
  }

  @Override
  public boolean contains(Material material, int i) throws IllegalArgumentException {
    return false;
  }

  @Override
  public boolean contains(ItemStack itemStack, int i) {
    return false;
  }

  @Override
  public boolean containsAtLeast(ItemStack itemStack, int i) {
    return false;
  }

  @Override
  public HashMap<Integer, ? extends ItemStack> all(int i) {
    return Maps.newHashMap();
  }

  @Override
  public HashMap<Integer, ? extends ItemStack> all(Material material) throws IllegalArgumentException {
    return Maps.newHashMap();
  }

  @Override
  public HashMap<Integer, ? extends ItemStack> all(ItemStack itemStack) {
    return Maps.newHashMap();
  }

  @Override
  public int first(int i) {
    return -1;
  }

  @Override
  public int first(Material material) throws IllegalArgumentException {
    return -1;
  }

  @Override
  public int first(ItemStack itemStack) {
    return -1;
  }

  @Override
  public int firstEmpty() {
    return -1;
  }

  @Override
  public void remove(int i) {

  }

  @Override
  public void remove(Material material) throws IllegalArgumentException {

  }

  @Override
  public void remove(ItemStack itemStack) {

  }

  @Override
  public void clear(int i) {
		setItem(i, null);
  }

  @Override
  public void clear() {
		java.util.Arrays.fill(storage, null);
		java.util.Arrays.fill(armor, null);
		java.util.Arrays.fill(extra, null);
  }

  @Override
  public List<HumanEntity> getViewers() {
    return Collections.emptyList();
  }

  @Override
  public String getTitle() {
    return "MockEmptyInventory";
  }

  @Override
  public InventoryType getType() {
    return InventoryType.PLAYER;
  }

  @Override
  public void setArmorContents(ItemStack[] itemStacks) {
		java.util.Arrays.fill(armor, null);
		System.arraycopy(itemStacks, 0, armor, 0, min(armor.length, itemStacks.length));
  }

  @Override
  public void setExtraContents(ItemStack[] itemStacks) {
		java.util.Arrays.fill(extra, null);
		System.arraycopy(itemStacks, 0, extra, 0, min(extra.length, itemStacks.length));
  }

  @Override
  public void setHelmet(ItemStack itemStack) {
		armor[3] = itemStack;
  }

  @Override
  public void setChestplate(ItemStack itemStack) {
		armor[2] = itemStack;
  }

  @Override
  public void setLeggings(ItemStack itemStack) {
		armor[1] = itemStack;
  }

  @Override
  public void setBoots(ItemStack itemStack) {
		armor[0] = itemStack;
  }

  @Override
  public ItemStack getItemInMainHand() {
		return getItem(heldItemSlot);
  }

  @Override
  public void setItemInMainHand(ItemStack itemStack) {
		setItem(heldItemSlot, itemStack);
  }

  @Override
  public ItemStack getItemInOffHand() {
		return extra[0];
  }

  @Override
  public void setItemInOffHand(ItemStack itemStack) {
		extra[0] = itemStack;
  }

  @Override
  public ItemStack getItemInHand() {
		return getItemInMainHand();
  }

  @Override
  public void setItemInHand(ItemStack itemStack) {
		setItemInMainHand(itemStack);
  }

  @Override
  public int getHeldItemSlot() {
		return heldItemSlot;
  }

  @Override
  public void setHeldItemSlot(int i) {
		if (i >= 0 && i < 9) {
			heldItemSlot = i;
		}
  }

  @Override
  public int clear(int i, int i1) {
    return 0;
  }

  @Override
  public HumanEntity getHolder() {
    return null;
  }

  @Override
  public ListIterator<ItemStack> iterator() {
    return null;
  }

  @Override
  public ListIterator<ItemStack> iterator(int i) {
    return null;
  }

  @Override
  public Location getLocation() {
    return null;
  }
}
