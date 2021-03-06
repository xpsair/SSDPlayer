/*******************************************************************************
 * SSDPlayer Visualization Platform (Version 1.0)
 * Authors: Or Mauda, Roman Shor, Gala Yadgar, Eitan Yaakobi, Assaf Schuster
 * Copyright (c) 2015, Technion � Israel Institute of Technology
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that
 * the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
 *******************************************************************************/
package entities;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;

import utils.Utils;

/**
 * November 2015: revised by Or Mauda for additional RAID functionality.
 */
public abstract class Device<P extends Page, B extends Block<P>, T extends Plane<P,B>, C extends Chip<P,B,T>> {	
	public abstract static class Builder<P extends Page, B extends Block<P>, T extends Plane<P,B>, D extends Chip<P,B,T>> {
		private Device<P,B,T,D> device;

		abstract public Device<P,B,T,D> build();
		
		public Builder<P,B,T,D> setTotalMoved(int totalMoved) {
			device.totalMoved = totalMoved;
			return this;
		}
		
		public Builder<P,B,T,D> setTotalWritten(int totalWritten) {
			device.totalWritten = totalWritten;
			return this;
		}
		
		public Builder<P,B,T,D> setChips(List<D> chipsList) {
			device.chipsList = new ArrayList<D>(chipsList);
			return this;
		}
		
		protected void validate() {
			Utils.validateNotNull(device.chipsList, "Chips");
		}
		
		protected void setDevice(Device<P,B,T,D> device) {
			this.device = device;
		}
	}
	
	private List<C> chipsList = null;
	private int totalMoved = 0;
	private int totalWritten = 0;
	
	protected Device() {}	
	
	protected Device(Device<P,B,T,C> other) {
		this.chipsList = new ArrayList<C>(other.chipsList);
		this.totalMoved = other.totalMoved; 
		this.totalWritten = other.totalWritten; 
	}

	abstract public Builder<P,B,T,C> getSelfBuilder();

	public Iterable<C> getChips() {
		return chipsList;
	}
	
	public C getChip(int i) {
		return chipsList.get(i);
	}

	public List<C> getNewChipsList() {
		return new ArrayList<C>(chipsList);
	}
	
	public int getTotalMoved() {
		return totalMoved;
	}
	
	public int getTotalWritten() {
		return totalWritten;
	}

	@SuppressWarnings("unchecked")
	public Device<P,B,T,C> invokeCleaning() {
		int moved = 0;
		List<C> cleanChips = new ArrayList<C>(getChipsNum());
		for (C chip : getChips()) {
			Pair<Chip<P,B,T>,Integer> clean = chip.clean();
			moved += clean.getValue1();
			cleanChips.add((C) clean.getValue0());
		}
		Builder<P,B,T,C> builder = getSelfBuilder();
		builder.setChips(cleanChips).setTotalMoved(totalMoved + moved);
		return builder.build();
	}

	@SuppressWarnings("unchecked")
	public Device<P,B,T,C>  invalidate(int lp) {
		List<C> updatedChips = getNewChipsList();
		int chipIndex = getChipIndex(lp);
		C chip = getChip(chipIndex);
		updatedChips.set(chipIndex, (C) chip.invalidate(lp));
		Builder<P,B,T,C>  builder = getSelfBuilder();
		builder.setChips(updatedChips);
		return builder.build();
	}

	@SuppressWarnings("unchecked")
	public Device<P,B,T,C> writeLP(int lp, int arg) {
		int chipIndex = getChipIndex(lp);
		List<C> updatedChips = getNewChipsList();
		updatedChips.set(chipIndex, (C) getChip(chipIndex).writeLP(lp, arg));
		Builder<P, B, T, C> builder = getSelfBuilder();
		builder.setChips(updatedChips).setTotalWritten(totalWritten + 1);
		return builder.build();
	}
	
	protected int getChipsNum() {
		return chipsList.size();
	}
	
	protected int getChipIndex(int lp) {
		return lp%getChipsNum();
	}
}
