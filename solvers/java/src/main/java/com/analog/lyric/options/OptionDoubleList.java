/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

package com.analog.lyric.options;

import net.jcip.annotations.Immutable;

import com.google.common.primitives.Doubles;


/**
 * Represents list of doubles for use as option value.
 * @since 0.07
 * @author Christopher Barber
 */
@Immutable
public class OptionDoubleList extends AbstractOptionValueList<Double>
{
	private static final long serialVersionUID = 1L;

	final public static OptionDoubleList EMPTY = new OptionDoubleList();
	
	/**
	 * @param elements
	 * @since 0.07
	 */
	public OptionDoubleList(Double[] elements)
	{
		super(Double.class, elements);
	}

	public OptionDoubleList(double ... elements)
	{
		super(Doubles.asList(elements).toArray(new Double[elements.length]));
	}
	
	@Override
	public double[] toPrimitiveArray()
	{
		return Doubles.toArray(this);
	}
}