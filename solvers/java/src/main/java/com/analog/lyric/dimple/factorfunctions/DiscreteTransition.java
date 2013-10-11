/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

package com.analog.lyric.dimple.factorfunctions;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;


/**
 * Parameterized discrete transition factor, which corresponds to p(y | x, A),
 * where A is a matrix of transition probabilities that are
 * parameterized as (not necessarily normalized) probabilities.
 * The transition matrix is organized such that each column is represented as
 * a single RealJoint variable that corresponds to
 * the output distribution for each input state. That is, the transition
 * matrix multiplies on the left.  The domain of x and y do not need to be
 * the same.
 * 
 * Representing A as described, the conjugate prior for A is such that
 * each column vector of the A matrix is distributed according to
 * a Dirichlet distribution.
 * Depending on the solver, it may or may not be necessary to use a
 * conjugate prior (for the Gibbs solver, for example, it is not).
 * 
 * The variables in the argument list are ordered as follows:
 * 
 * y: Discrete output variable (MUST be zero-based integer values) 	// TODO: remove this restriction
 * x: Discrete input variable (MUST be zero-based integer values) 	// TODO: remove this restriction
 * A: Matrix of transition matrix values (1D vector of RealJoint variables)
 * 
 */
public class DiscreteTransition extends FactorFunction
{
	private final static int NUM_DATA_ARGUMENTS = 2;
	
	@Override
	public double evalEnergy(Object... arguments)
    {
    	if (arguments.length < NUM_DATA_ARGUMENTS + 1)
    		throw new DimpleException("Insufficient number of arguments.");
    	
    	int y = FactorFunctionUtilities.toInteger(arguments[0]);			// First argument is y (output variable)
    	int x = FactorFunctionUtilities.toInteger(arguments[1]);			// Second argument is x (input variable)

    	double[] Acol = (double[])arguments[x + NUM_DATA_ARGUMENTS];		// Choose column of A indexed by input variable x
    	double yDimension = Acol.length;
    	
    	double sum = 0;									// Normalize over column selected by input variable x
    	for (int row = 0; row < yDimension; row++)
    	{
    		double a = Acol[row];
    		if (a < 0)
    			return Double.POSITIVE_INFINITY;
    		sum += a;
    	}
    	
    	return -Math.log(Acol[y]) + Math.log(sum);
	}

}
