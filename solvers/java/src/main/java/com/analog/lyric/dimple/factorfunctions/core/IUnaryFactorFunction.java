/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

package com.analog.lyric.dimple.factorfunctions.core;

import com.analog.lyric.dimple.data.IDatum;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.util.misc.Matlab;

/**
 * A factor function that can be called with a single argument.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public interface IUnaryFactorFunction extends IDatum
{
	@Override
	IUnaryFactorFunction clone();
	
	/**
	 * Computes the energy produced by the function for the given value.
	 * <p>
	 * The energy may be thought of either as a cost or as an unnormalized log probability.
	 * <p>
	 * @param value must have a type compatible with the function.
	 * @since 0.08
	 */
	public double evalEnergy(Value value);
	
	@Matlab
	public double evalEnergy(Object value);
}
