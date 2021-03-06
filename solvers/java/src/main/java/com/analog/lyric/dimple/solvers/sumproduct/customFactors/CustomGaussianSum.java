/*******************************************************************************
*   Copyright 2012-2014 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.sumproduct.customFactors;

import java.util.List;

import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.VariablePredicates;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolverGraph;
import com.google.common.collect.Iterables;


public class CustomGaussianSum extends GaussianFactorBase
{
	protected int _sumIndex;
	private int _sumPort;
	private double _constantSum;
	
	public CustomGaussianSum(Factor factor, SumProductSolverGraph parent)
	{
		super(factor, parent);
		_sumIndex = 0;		// Index that is the sum of all the others
		assertUnboundedReal(factor);
	}

	@Override
	public void doUpdateEdge(int outPortNum)
	{
		// uout = ua + ub + uc
		// ub = uout-ua-uc
		// sigma^2 = othersigma^2 + theothersigma^2 ...
		
		if (outPortNum == _sumPort)
			updateSumEdge();
		else
			updateSummandEdge(outPortNum);
	}
	
	private void updateSumEdge()
	{
		double mean = _constantSum;
		double variance = 0;
		
		for (int i = 0, n = getSiblingCount(); i < n; i++)
		{
			if (i != _sumPort)
			{
				NormalParameters msg = getSiblingEdgeState(i).varToFactorMsg;
				mean += msg.getMean();
				variance += msg.getVariance();
			}
		}

		NormalParameters outMsg = getSiblingEdgeState(_sumPort).factorToVarMsg;
		outMsg.setMean(mean);
		outMsg.setVariance(variance);
	}
	
	
	private void updateSummandEdge(int outPortNum)
	{
		
		double mean = -_constantSum;		// For summands, use negative of constant sum
		double variance = 0;
		
		for (int i = 0, n = getSiblingCount(); i < n; i++)
		{
			if (i != outPortNum)
			{
				NormalParameters msg = getSiblingEdgeState(i).varToFactorMsg;
				if (i == _sumPort)
					mean += msg.getMean();
				else
					mean -= msg.getMean();

				variance += msg.getVariance();
			}
		}

		NormalParameters outMsg = getSiblingEdgeState(outPortNum).factorToVarMsg;
		outMsg.setMean(mean);
		outMsg.setVariance(variance);
	}


	@Override
	public void initialize()
	{
		super.initialize();
		
		// Pre-compute sum associated with any constant edges
		final Factor factor = _model;
		_sumPort = factor.hasConstantAtIndex(_sumIndex) ? -1 : _sumIndex;	// If sum isn't a variable, then set port to invalid value
		_constantSum = 0;
		if (_model.hasConstants())
		{
			List<Value> constantValues = factor.getConstantValues();
			int[] constantIndices = factor.getConstantIndices();
			for (int i = 0, n = constantValues.size(); i < n; i++)
			{
				double constant = constantValues.get(i).getDouble();
				if (constantIndices[i] == _sumIndex)
					_constantSum -= constant;	// Constant sum value counts as negative
				else
					_constantSum += constant;	// Constant summand value counts as positive
			}
		}

	}
	
	
	/**
	 * Utility to indicate whether or not a factor is compatible with the requirements of this custom factor
	 * @deprecated as of release 0.08
	 */
	@Deprecated
	public static boolean isFactorCompatible(Factor factor)
	{
		return Iterables.all(factor.getSiblings(), VariablePredicates.isUnboundedReal());
	}


}
