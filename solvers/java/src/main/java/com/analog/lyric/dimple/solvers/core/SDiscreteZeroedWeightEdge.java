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

package com.analog.lyric.dimple.solvers.core;

import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteWeightMessage;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class SDiscreteZeroedWeightEdge extends SDiscreteWeightEdge
{
	public SDiscreteZeroedWeightEdge(DiscreteWeightMessage varToFactorMsg, DiscreteWeightMessage factorToVarMsg)
	{
		super(varToFactorMsg, factorToVarMsg);
	}

	public SDiscreteZeroedWeightEdge(int size)
	{
		super(new DiscreteWeightMessage(size), new DiscreteWeightMessage(size));
	}
	
	public SDiscreteZeroedWeightEdge(Discrete var)
	{
		this(var.getDomain().size());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation sets all message weights to zero instead of normalizing them
	 * to sum to one.
	 */
	@Override
	public void reset()
	{
		varToFactorMsg.setWeightsToZero();
		factorToVarMsg.setWeightsToZero();
	}
}