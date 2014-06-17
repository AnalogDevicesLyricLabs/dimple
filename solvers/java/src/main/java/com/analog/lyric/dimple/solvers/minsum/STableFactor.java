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

package com.analog.lyric.dimple.solvers.minsum;

import java.util.Arrays;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.Selection;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorTableRepresentation;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.core.STableFactorDoubleArray;
import com.analog.lyric.dimple.solvers.core.kbest.IKBestFactor;
import com.analog.lyric.dimple.solvers.core.kbest.KBestFactorEngine;
import com.analog.lyric.dimple.solvers.core.kbest.KBestFactorTableEngine;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteEnergyMessage;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.optimizedupdate.FactorUpdatePlan;
import com.analog.lyric.dimple.solvers.optimizedupdate.ISTableFactorSupportingOptimizedUpdate;
import com.analog.lyric.dimple.solvers.optimizedupdate.STableFactorOptimizedUpdateImpl;
import com.analog.lyric.dimple.solvers.optimizedupdate.UpdateApproach;
import com.analog.lyric.util.misc.Nullable;

public class STableFactor extends STableFactorDoubleArray implements IKBestFactor, ISTableFactorSupportingOptimizedUpdate
{
	/*
	 * We cache all of the double arrays we use during the update.  This saves
	 * time when performing the update.
	 */
    protected double [][] _savedOutMsgArray = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
    protected double [] _dampingParams = ArrayUtil.EMPTY_DOUBLE_ARRAY;
    protected int _k;
    @Nullable
    private TableFactorEngine _tableFactorEngine;
    protected KBestFactorEngine _kbestFactorEngine;
    protected boolean _kIsSmallerThanDomain;
    protected boolean _dampingInUse = false;
    protected final STableFactorOptimizedUpdateImpl _sTableFactorOptimizedUpdateImpl;

    /*--------------
     * Construction
     */
    
    public STableFactor(Factor factor)
	{
    	super(factor);
    	
		SFactorGraph sfg = (SFactorGraph) getRootGraph();
		if (sfg == null)
		{
			throw new DimpleException("Internal error.");
		}
		_sTableFactorOptimizedUpdateImpl = new STableFactorOptimizedUpdateImpl(sfg._optimizedUpdateImpl, getFactorTable());
    	
		
		if (factor.getFactorFunction().factorTableExists(getFactor()))
			_kbestFactorEngine = new KBestFactorTableEngine(this);
		else
			_kbestFactorEngine = new KBestFactorEngine(this);
	}
    
	@Override
	public void initialize()
	{
		super.initialize();
    	configureDampingFromOptions();
	    updateK(getOptionOrDefault(MinSumOptions.maxMessageSize));
		
		if (_sTableFactorOptimizedUpdateImpl.useOptimizedUpdate())
		{
			FactorUpdatePlan factorUpdatePlan = _sTableFactorOptimizedUpdateImpl.getOptimizedUpdatePlan();
			_tableFactorEngine = new TableFactorEngineOptimized(this, factorUpdatePlan);
		}
		else
		{
			_tableFactorEngine = new TableFactorEngine(this);
		}
	}

    /*---------------------
     * ISolverNode methods
     */
   
	@Override
	public void moveMessages(ISolverNode other, int portNum, int otherPort)
	{
		super.moveMessages(other,portNum,otherPort);
	    if (_dampingInUse)
	    	_savedOutMsgArray[portNum] = ((STableFactor)other)._savedOutMsgArray[otherPort];
	    
	}

	private TableFactorEngine getTableFactorEngine()
	{
		final TableFactorEngine tableFactorEngine = _tableFactorEngine;
		if (tableFactorEngine != null)
		{
			return tableFactorEngine;
		}
		else
		{
			throw new DimpleException("The solver was not initialized. Use solve() or call initialize() before iterate().");
		}
	}

	@Override
	protected void doUpdate()
	{
		if (_kIsSmallerThanDomain)
			_kbestFactorEngine.update();
		else
			getTableFactorEngine().update();
	}
	
	@Override
	public void doUpdateEdge(int outPortNum)
	{
		if (_kIsSmallerThanDomain)
			_kbestFactorEngine.updateEdge(outPortNum);
		else
			getTableFactorEngine().updateEdge(outPortNum);

	}
	
	public int getK()
	{
		return _k;
	}
	
	public void setK(int k)
	{
		setOption(MinSumOptions.maxMessageSize, k);
		updateK(k);
	}
	
	private void updateK(int k)
	{
		if (k != _k)
		{
			_k = k;
			_kbestFactorEngine.setK(k);
			_kIsSmallerThanDomain = false;
			for (int i = 0; i < _inputMsgs.length; i++)
			{
				if (_k < _inputMsgs[i].length)
				{
					_kIsSmallerThanDomain = true;
					break;
				}
			}
		}
	}
    
	/*-----------------------
	 * ISolverFactor methods
	 */

	@Override
	public void createMessages()
	{
		super.createMessages();
	}

	/*--------------------------
	 * STableFactorBase methods
	 */
	
	@Override
	protected void setTableRepresentation(IFactorTable table)
	{
		table.setRepresentation(FactorTableRepresentation.SPARSE_ENERGY_WITH_INDICES);
	}

	/**
	 * Gets the update algorithm approach.
	 * 
	 * @since 0.07
	 */
	public UpdateApproach getUpdateApproach()
	{
		return _sTableFactorOptimizedUpdateImpl.getUpdateApproach();
	}
	
	/**
	 * Sets the update algorithm approach.
	 * 
	 * @since 0.07
	 */
	public void setUpdateApproach(UpdateApproach approach)
	{
		_sTableFactorOptimizedUpdateImpl.setUpdateApproach(approach);
	}

	/**
	 * Reverts to the inherited setting for the update algorithm approach.
	 * 
	 * @since 0.07
	 */
	public void unsetUpdateApproach()
	{
		_sTableFactorOptimizedUpdateImpl.unsetUpdateApproach();
	}

	public boolean getAutomaticOptimizationDecision()
	{
		return _sTableFactorOptimizedUpdateImpl.getAutomaticOptimizationDecision();
	}
	
	/**
	 * Returns the effective update approach for the factor. If the update approach is set to
	 * automatic, this value is not valid until the graph is initialized. Note that a factor
	 * with only one edge always employs the normal update approach.
	 * 
	 * @since 0.07
	 */
	public UpdateApproach getEffectiveUpdateApproach()
	{
		if (_sTableFactorOptimizedUpdateImpl.useOptimizedUpdate())
		{
			return UpdateApproach.UPDATE_APPROACH_OPTIMIZED;
		}
		else
		{
			return UpdateApproach.UPDATE_APPROACH_NORMAL;
		}
	}

	/**
	 * Gets the optimized update sparse threshold.
	 * 
	 * @see #setOptimizedUpdateSparseThreshold(double)
	 * @since 0.07
	 */
	public double getOptimizedUpdateSparseThreshold()
	{
		return _sTableFactorOptimizedUpdateImpl.getOptimizedUpdateSparseThreshold();
	}
	
	/**
	 * Sets the optimized update sparse threshold. The optimized update algorithm uses auxiliary
	 * factor tables during update. This density setting determines whether it uses sparse or dense
	 * representations for them. Sparse representations often offer superior execution time, but use
	 * more memory because indices are stored. The automatic update approach considers the impact of
	 * this setting when estimating update cost for the optimized update algorithm.
	 * 
	 * @param value A density, below which the system uses a sparse representation for auxiliary
	 *        factor tables.
	 * @since 0.07
	 */
	public void setOptimizedUpdateSparseThreshold(double value)
	{
		_sTableFactorOptimizedUpdateImpl.setOptimizedUpdateSparseThreshold(value);
	}

	/**
	 * Resets the optimized update sparse threshold to use the inherited value.
	 * 
	 * @see #setOptimizedUpdateSparseThreshold(double)
	 * @since 0.07
	 */
	public void unsetOptimizedUpdateSparseThreshold()
	{
		_sTableFactorOptimizedUpdateImpl.unsetOptimizedUpdateSparseThreshold();
	}
	
	/**
	 * Gets the automatic memory allocation scaling factor.
	 * 
	 * @see #setAutomaticMemoryAllocationScalingFactor(double)
	 * @since 0.07
	 */
	public double getAutomaticMemoryAllocationScalingFactor()
	{
		return _sTableFactorOptimizedUpdateImpl.getAutomaticMemoryAllocationScalingFactor();
	}
	
	/**
	 * When the update approach is automatic, the system chooses which update algorithm to use by
	 * estimating the execution time and memory allocation of each. The memory allocation estimate
	 * is scaled by this factor in the cost estimation.
	 * 
	 * @since 0.07
	 */
	public void setAutomaticMemoryAllocationScalingFactor(double value)
	{
		_sTableFactorOptimizedUpdateImpl.setAutomaticMemoryAllocationScalingFactor(value);
	}
	
	/**
	 * Resets the automatic memory allocation scaling factor to use the inherited value.
	 * 
	 * @see #setAutomaticMemoryAllocationScalingFactor(double)
	 * @since 0.07
	 */
	public void unsetAutomaticMemoryAllocationScalingFactor()
	{
		_sTableFactorOptimizedUpdateImpl.unsetAutomaticMemoryAllocationScalingFactor();
	}

	/**
	 * Gets the automatic execution time scaling factor.
	 * 
	 * @see #setAutomaticExecutionTimeScalingFactor(double)
	 * @since 0.07
	 */
	public double getAutomaticExecutionTimeScalingFactor()
	{
		return _sTableFactorOptimizedUpdateImpl.getAutomaticExecutionTimeScalingFactor();
	}
	
	/**
	 * When the update approach is automatic, the system chooses which update algorithm to use by
	 * estimating the execution time and memory allocation of each. The execution time estimate
	 * is scaled by this factor in the cost estimation.
	 * 
	 * @since 0.07
	 */
	public void setAutomaticExecutionTimeScalingFactor(double value)
	{
		_sTableFactorOptimizedUpdateImpl.setAutomaticExecutionTimeScalingFactor(value);
	}
	
	/**
	 * Resets the automatic execution time scaling factor to use the inherited value.
	 * 
	 * @see #setAutomaticExecutionTimeScalingFactor(double)
	 * @since 0.07
	 */
	public void unsetAutomaticExecutionTimeScalingFactor()
	{
		_sTableFactorOptimizedUpdateImpl.unsetAutomaticExecutionTimeScalingFactor();
	}
    
	/*----------------------
	 * IKBestFactor methods
	 */
	@Override
	public FactorFunction getFactorFunction()
	{
		return getFactor().getFactorFunction();
	}

	@Override
	public double initAccumulator()
	{
		return 0;
	}

	@Override
	public double accumulate(double oldVal, double newVal)
	{
		return oldVal + newVal;
	}

	@Override
	public double combine(double oldVal, double newVal)
	{
		if (oldVal < newVal)
			return oldVal;
		else
			return newVal;
	}

	@Override
	public void normalize(double[] outputMsg)
	{
		double minVal = Double.POSITIVE_INFINITY;
		
		for (int i = 0; i < outputMsg.length; i++)
			if (outputMsg[i] < minVal)
				minVal = outputMsg[i];
		
		for (int i = 0; i < outputMsg.length; i++)
			outputMsg[i] -= minVal;
	}

	@Override
	public double evalFactorFunction(Object[] inputs)
	{
		return getFactorFunction().evalEnergy(inputs);
	}

	@Override
	public void initMsg(double[] msg)
	{
		Arrays.fill(msg, Double.POSITIVE_INFINITY);
	}

	@Override
	public double getFactorTableValue(int index)
	{
		return getFactorTable().getEnergiesSparseUnsafe()[index];
	}
	
	@Override
	public int[] findKBestForMsg(double[] msg, int k)
	{
		return Selection.findFirstKIndices(msg, k);
	}


	@Override
	public double[][] getInPortMsgs()
	{
		return _inputMsgs;
	}


	@Override
	public double[][] getOutPortMsgs()
	{
		return _outputMsgs;
	}
	
	/*---------------
	 * SNode methods
	 */
	
	@Override
	protected DiscreteEnergyMessage cloneMessage(int edge)
	{
		return new DiscreteEnergyMessage(_outputMsgs[edge]);
	}
	
	@Override
	protected boolean supportsMessageEvents()
	{
		return true;
	}

	/*-------------
     * New methods
     */
	
	/**
	 * @deprecated Use {@link MinSumOptions#damping} or {@link MinSumOptions#nodeSpecificDamping} options instead.
	 */
	@Deprecated
	public void setDamping(int index, double val)
	{
		double[] params  = MinSumOptions.nodeSpecificDamping.getOrDefault(this).toPrimitiveArray();
		if (params.length == 0 && val != 0.0)
		{
			params = new double[getSiblingCount()];
		}
		if (params.length != 0)
		{
			params[index] = val;
		}
		
		MinSumOptions.nodeSpecificDamping.set(this, params);
		configureDampingFromOptions();
	}
		
	@Override
	public double getDamping(int index)
	{
		return _dampingParams.length > 0 ? _dampingParams[index] : 0.0;
	}

	@Override
	public boolean isDampingInUse()
	{
		return _dampingInUse;
	}

	@Override
	public double[] getSavedOutMsgArray(int _outPortNum)
	{
		return _savedOutMsgArray[_outPortNum];
	}

	/*------------------
	 * Internal methods
	 */
	
    protected void configureDampingFromOptions()
    {
     	final int size = getSiblingCount();
    	
    	_dampingParams =
    		getReplicatedNonZeroListFromOptions(MinSumOptions.nodeSpecificDamping, MinSumOptions.damping,
    			size, _dampingParams);
 
    	if (_dampingParams.length > 0 && _dampingParams.length != size)
    	{
			DimpleEnvironment.logWarning("%s has wrong number of parameters for %s\n",
				MinSumOptions.nodeSpecificDamping, this);
    		_dampingParams = ArrayUtil.EMPTY_DOUBLE_ARRAY;
    	}
    	
    	_dampingInUse = _dampingParams.length > 0;
    	
    	configureSavedMessages(size);
    }
    
    protected void configureSavedMessages(int size)
    {
    	if (!_dampingInUse)
    	{
    		_savedOutMsgArray = ArrayUtil.EMPTY_DOUBLE_ARRAY_ARRAY;
    	}
    	else if (_savedOutMsgArray.length != size)
    	{
    		_savedOutMsgArray = new double[size][];
    		for (int i = 0; i < size; i++)
    	    {
    			_savedOutMsgArray[i] = new double[_inputMsgs[i].length];
    	    }
    	}
    }


}

