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

import java.util.Collection;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.values.IndexedValue;
import com.analog.lyric.dimple.model.values.Value;


/**
 * Deterministic matrix product. This is a deterministic directed factor
 * (if smoothing is not enabled).
 * 
 * The constructor has three arguments that specify the sizes of the input and output
 * matrices.  The first two are the number of rows and columns, respectively, of the
 * first input matrix.  The third is the number of columns of the second input matrix.
 * The number of rows of the second input matrix must equal the number of columns of the
 * first input matrix.
 * 
 * Optional smoothing may be applied, by providing a smoothing value in the
 * constructor. If smoothing is enabled, the distribution is smoothed by
 * exp(-difference^2/smoothing), where difference is the distance between the
 * output value and the deterministic output value for the corresponding inputs.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1) Output matrix (Nr x Nc, scanned by columns [because MATLAB assumes this])
 * 2) Input matrix 1 (Nr x Nx, scanned by columns [because MATLAB assumes this])
 * 3) Input matrix 2 (Nx x Nc, scanned by columns [because MATLAB assumes this])
 * 
 */
public class MatrixProduct extends FactorFunction
{
	protected int _Nr;
	protected int _Nx;
	protected int _Nc;
	protected double[][] _in1;
	protected double[][] _in2;
	protected double[][] _out;
	protected double _beta = 0;
	protected boolean _smoothingSpecified = false;
	private final int _updateDeterministicLimit;
	
	public MatrixProduct(int Nr, int Nx, int Nc) {this(Nr, Nx, Nc, 0);}
	public MatrixProduct(int Nr, int Nx, int Nc, double smoothing)
	{
		super();
		_Nr = Nr;
		_Nx = Nx;
		_Nc = Nc;
		_in1 = new double[Nr][Nx];
		_in2 = new double[Nx][Nc];
		_out = new double[Nr][Nc];

		if (smoothing > 0)
		{
			_beta = 1 / smoothing;
			_smoothingSpecified = true;
			_updateDeterministicLimit = 0;
		}
		else
		{
			// A full update costs Nr*Nx*Nc multiply/adds. An incremental update will cost either
			// 2*Nr or 2*Nc depending on which input matrix contains the changed variable.
			_updateDeterministicLimit = (Nr * Nx * Nc) / (2 * Math.max(Nr, Nc));
		}
	}
	
    @Override
    public double evalEnergy(Object ... arguments)
    {
    	final int Nr = _Nr;
    	final int Nx = _Nx;
    	final int Nc = _Nc;
    	final double[][] out = _out;
    	double[][] in1 = _in1;
    	double[][] in2 = _in2;

    	int argIndex = 0;
    	
		// Get the output matrix values
    	for (int c = 0; c < Nc; c++)		// Scan by columns
    		for (int r = 0; r < Nr; r++)
    			out[r][c] = FactorFunctionUtilities.toDouble(arguments[argIndex++]);
    	
		// Get the first input matrix values
    	if (arguments[argIndex] instanceof double[][])	// Constant matrix is passed as a single argument
    		in1 = (double[][])arguments[argIndex++];
    	else
    	{
    		for (int x = 0; x < Nx; x++)		// Scan by columns
    			for (int r = 0; r < Nr; r++)
    				in1[r][x] = FactorFunctionUtilities.toDouble(arguments[argIndex++]);
    	}

		// Get the second input matrix values
    	if (arguments[argIndex] instanceof double[][])	// Constant matrix is passed as a single argument
    		in2 = (double[][])arguments[argIndex++];
    	else
    	{
    		for (int c = 0; c < Nc; c++)		// Scan by columns
    			for (int x = 0; x < Nx; x++)
    				in2[x][c] = FactorFunctionUtilities.toDouble(arguments[argIndex++]);
    	}
    	
    	// Compute the expected output and total error
    	double error = 0;
    	for (int c = 0; c < Nc; c++)
    	{
    		for (int r = 0; r < Nr; r++)
    		{
    			double[] in1r = in1[r];
    			double sum = 0;
    			for (int x = 0; x < Nx; x++)
    				sum += in1r[x] * in2[x][c];
    			double diff = out[r][c] - sum;
    			error += diff*diff;
    		}
    	}

    	if (_smoothingSpecified)
    		return error*_beta;
    	else
    		return (error == 0) ? 0 : Double.POSITIVE_INFINITY;
    }
    
    
    @Override
    public final boolean isDirected() {return true;}
    @Override
	public final int[] getDirectedToIndices()
	{
    	int[] indexList = new int[_Nr * _Nc];
		for (int col = 0, i = 0; col < _Nc; col++)
			for (int row = 0; row < _Nr; row++, i++)
    		indexList[i] = i;
		return indexList;
	}
    @Override
	public final boolean isDeterministicDirected() {return !_smoothingSpecified;}
    @Override
	public final void evalDeterministicFunction(Object[] arguments)
    {
    	final int Nr = _Nr;
    	final int Nx = _Nx;
    	final int Nc = _Nc;
    	double[][] in1 = _in1;
    	double[][] in2 = _in2;

    	int argIndex = Nr * Nc;	// Skip the outputs

		// Get the first input matrix values
    	if (arguments[argIndex] instanceof double[][])	// Constant matrix is passed as a single argument
    		in1 = (double[][])arguments[argIndex++];
    	else
    	{
    		for (int x = 0; x < Nx; x++)		// Scan by columns
    			for (int r = 0; r < Nr; r++)
    				in1[r][x] = FactorFunctionUtilities.toDouble(arguments[argIndex++]);
    	}

		// Get the second input matrix values
    	if (arguments[argIndex] instanceof double[][])	// Constant matrix is passed as a single argument
    		in2 = (double[][])arguments[argIndex++];
    	else
    	{
    		for (int c = 0; c < Nc; c++)		// Scan by columns
    			for (int x = 0; x < Nx; x++)
    				in2[x][c] = FactorFunctionUtilities.toDouble(arguments[argIndex++]);
    	}
    	
    	// Compute the output and replace the output values
    	int outIndex = 0;
    	for (int c = 0; c < Nc; c++)		// Scan by columns
    	{
    		for (int r = 0; r < Nr; r++)
    		{
    			double[] in1r = in1[r];
    			double sum = 0;
    			for (int x = 0; x < Nx; x++)
    				sum += in1r[x] * in2[x][c];
    			arguments[outIndex++] = sum;
    		}
    	}

    }
    
    @Override
    public final int updateDeterministicLimit()
    {
    	return _updateDeterministicLimit;
    }
    
    @Override
    public final boolean updateDeterministic(Value[] values, Collection<IndexedValue> oldValues)
    {
    	boolean incremental = false;
    	
    	final int outRows = _Nr;
    	final int outCols = _Nc;
    	final int outSize = outRows * outCols;
    	
    	final int in1Rows = _Nr;
    	final int in1Cols = _Nx;
    	final int in1Size = in1Rows * in1Cols;
    	
    	final int in2Rows = _Nx;
    	final int in2Cols = _Nc;
    	
    	final int in1Offset = outSize;
    	final Object objAtIn1Offset = values[in1Offset].getObject();
    	final double[][] in1Matrix = objAtIn1Offset instanceof double[][] ? (double[][])objAtIn1Offset : null;
    	
    	final int in2Offset = in1Offset + (in1Matrix == null ? in1Size : 1);
    	final Object objAtIn2Offset = values[in2Offset].getObject();
    	final double[][] in2Matrix = objAtIn2Offset instanceof double[][] ? (double[][])objAtIn2Offset : null;
    	
    	final int minSupportedIndex = in1Matrix == null ? in1Offset : (in2Matrix == null ? in2Offset : values.length);
    	final int maxSupportedIndex = in2Matrix == null ? values.length : (in1Matrix == null ? in2Offset : in1Offset);
    	
    	doIncremental:
    	{
    		if (in1Matrix != null && in2Matrix != null)
    		{
    			break doIncremental;
    		}
    		
    		for (IndexedValue old : oldValues)
    		{
    			final int changedIndex = old.getIndex();
    			
    			if (changedIndex < in1Offset || changedIndex >= values.length)
    			{
					throw new IndexOutOfBoundsException();
    			}

    			if (changedIndex < minSupportedIndex || changedIndex >= maxSupportedIndex)
    			{
    				break doIncremental;
    			}

    			final double newInput = values[changedIndex].getDouble();
    			final double oldInput = old.getValue().getDouble();
    			if (newInput == oldInput)
    			{
    				continue;
    			}

    			if (changedIndex >= in2Offset)
    			{
    				// Second matrix cell changed - changes column of output matrix
    				int x = changedIndex - in2Offset;
    				final int col = x / in2Rows;
    				final int in2Row = x - col * in2Rows;
    				final int in1Col = in2Row;

    				for (int row = 0; row < outRows; ++row)
    				{
    					final int outIndex =  col * outRows + row;
    					final int in1Index = in1Offset + in1Col * in1Rows + row;
    						
    					final double oldOutput = values[outIndex].getDouble();
    					final double in1Value = in1Matrix != null ? in1Matrix[row][in1Col] : values[in1Index].getDouble();
    					values[outIndex].setDouble(oldOutput - in1Value * oldInput + in1Value * newInput);
    				}
    			}
    			else
    			{
    				// First matrix cell changed - changes row of output matrix
    				int x = changedIndex - in1Offset;
    				final int in1Col = x / in1Rows;
    				final int row = x - in1Col * in1Rows;
    				final int in2Row = in1Col;
    				
    				for (int col = 0; col < outCols; ++col)
    				{
    					final int outIndex = col * outRows + row;
    					final int in2Index = in2Offset + col * in2Rows + in2Row;
    					
    					final double oldOutput = values[outIndex].getDouble();
    					final double in2Value = in2Matrix != null ? in2Matrix[in2Row][col] : values[in2Index].getDouble();
    					values[outIndex].setDouble(oldOutput - in2Value * oldInput + in2Value * newInput);
    				}
    			}
    		}
    		incremental = true;
    	}
    	
    	return incremental || super.updateDeterministic(values, oldValues);
    }
}