/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

package com.analog.lyric.dimple.FactorFunctions;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.DimpleException;


/**
 * Factor for an exchangeable set of Normally distributed variables associated
 * with a variable representing the mean parameter and a second parameter
 * representing the inverse variance. The variables are ordered as follows in
 * the argument list:
 * 
 * 1) Mean parameter (real variable)
 * 2) Inverse variance parameter (real variable; domain must be non-negative)
 * 3...) An arbitrary number of real variables.
 * 
 * Mean and standard-deviation parameters may optionally be specified as constants in the constructor.
 * In this case, the mean and inverse-variance are not included in the list of arguments.
 * 
 */
public class Normal extends FactorFunction
{
	double _mean;
	double _inverseVariance;
	double _logInverseVarianceOverTwo;
	boolean _meanSpecified = false;
	boolean _inverseVarianceSpecified = false;

	public Normal() {super("Normal");}
	public Normal(double mean, double standardDeviation)
	{
		this();
		_mean = mean;
		_meanSpecified = true;
		_inverseVariance = 1/(standardDeviation*standardDeviation);
		_logInverseVarianceOverTwo = Math.log(_inverseVariance)/2;
		_inverseVarianceSpecified = true;
    	if (_inverseVariance < 0) throw new DimpleException("Negative inverse variance value. Domain must be restricted to non-negative values.");
	}

    @Override
	public double evalEnergy(Object ... arguments)
	{
    	int index = 0;
    	if (!_meanSpecified)
    		_mean = FactorFunctionUtilities.toDouble(arguments[index++]);				// First variable is mean parameter
    	if (!_inverseVarianceSpecified)
    	{
    		_inverseVariance = FactorFunctionUtilities.toDouble(arguments[index++]);	// Second variable is inverse variance (must be non-negative)
    		_logInverseVarianceOverTwo = Math.log(_inverseVariance)/2;
    		if (_inverseVariance < 0) throw new DimpleException("Negative inverse variance value. Domain must be restricted to non-negative values.");
    	}
    	int length = arguments.length;
    	int N = length - index;									// Number of Normal variables
    	double sum = 0;
    	for (; index < length; index++)
    	{
    		double relInput = FactorFunctionUtilities.toDouble(arguments[index]) - _mean;	// Remaining inputs are Normal variables
    		sum += relInput*relInput*_inverseVariance;
    	}

    	return sum/2 - N * _logInverseVarianceOverTwo;
	}
}
