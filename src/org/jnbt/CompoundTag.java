package org.jnbt;

/*
 * JNBT License
 * 
 * Copyright (c) 2010 Graham Edgecombe
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *       
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *       
 *     * Neither the name of the JNBT team nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.v1_4_R1.NBTBase;
import net.minecraft.server.v1_4_R1.NBTTagCompound;

/**
 * The <code>TAG_Compound</code> tag.
 * @author Graham Edgecombe
 *
 */
public final class CompoundTag extends Tag {
	
	/**
	 * The value.
	 */
	private final Map<String, Tag> value;
	
	/**
	 * Creates the tag.
	 * @param name The name.
	 * @param value The value.
	 */
	public CompoundTag(String name, Map<String, Tag> value) {
		super(name, TagType.COMPOUND);
		this.value = value;
	}

	@Override
	public Map<String, Tag> getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		String name = getName();
		String append = "";
		if(name != null && !name.equals("")) {
			append = "(\"" + this.getName() + "\")";
		}
		StringBuilder bldr = new StringBuilder();
		bldr.append("TAG_Compound" + append + ": " + value.size() + " entries\r\n{\r\n");
		for(Map.Entry<String, Tag> entry : value.entrySet()) {
			bldr.append("   " + entry.getValue().toString().replaceAll("\r\n", "\r\n   ") + "\r\n");
		}
		bldr.append("}");
		return bldr.toString();
	}
	
	/**
	 * Takes an object and extracts it's CompoundTag counterpart.
	 * @param o The object to extract
	 * @return The extracted CompoundTag
	 */
	public static CompoundTag fromObject(Object o)
	{
		return NBTUtils.objectToCompoundTag(o, "");
	}
	
	/**
	 * Populates the given object's fields with the contents of
	 * this CompoundTag.
	 * @param o The object to populate
	 */
	public void toObject(Object o)
	{
		NBTUtils.populateObject(o, this);
	}

	@Override
	public NBTTagCompound toNBTTag()
	{
		NBTTagCompound tag = new NBTTagCompound(this.getName());
		
		for (Tag t : this.getValue().values())
		{
			tag.set(t.getName(), t.toNBTTag());
		}
		
		return tag;
	}
	
	@SuppressWarnings("unchecked")
	public static CompoundTag fromNBTTag(NBTTagCompound base)
	{
		CompoundTag tag = new CompoundTag(base.getName(), new HashMap<String, Tag>());
		
		for (NBTBase b : (Collection<? extends NBTBase>) base.c())
		{
			tag.getValue().put(b.getName(), Tag.fromNBTTag(b));
		}
		
		return tag;
	}

}
