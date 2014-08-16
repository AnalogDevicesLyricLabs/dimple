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

package com.analog.lyric.dimple.solvers.gibbs;

import java.util.Collection;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.repeated.BlastFromThePastFactor;
import com.analog.lyric.dimple.model.values.DiscreteValue;
import com.analog.lyric.dimple.model.values.IndexedValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.core.SBlastFromThePast;
import org.eclipse.jdt.annotation.Nullable;

public class STableFactorBlastFromThePast extends SBlastFromThePast implements ISolverFactorGibbs
{
    protected int _numPorts;
	private @Nullable double [] _outputMsg;
	private @Nullable DiscreteValue _inputMsg;
	private boolean _visited = false;
	
	public STableFactorBlastFromThePast(BlastFromThePastFactor f)
	{
		super(f);
	}

	@Override
	public void createMessages(VariableBase var, Port port)
	{
		super.createMessages(var,port);
		getMessages();
	}
	
	@SuppressWarnings("null")
	private void getMessages()
	{
		_outputMsg = (double[])getOtherVariablePort().node.getSolver().getInputMsg(getOtherVariablePort().index);
		_inputMsg = (DiscreteValue)getOtherVariablePort().node.getSolver().getOutputMsg(getOtherVariablePort().index);
		
	}
	
	@Override
	public void advance()
	{
		super.advance();
		getMessages();
	}

	@SuppressWarnings("null")
	@Override
	public double getPotential()
	{
		final DiscreteValue inputMsg = _inputMsg;
		final double[] outputMsg = _outputMsg;
		return inputMsg != null & outputMsg != null ? outputMsg[inputMsg.getIndex()] : Double.POSITIVE_INFINITY;
	}
	
	@Override
	public void updateNeighborVariableValue(int variableIndex, Value oldValue)
	{
		throw DimpleException.unsupportedMethod(getClass(), "updateNeighborVariableValue");
	}

	@Override
	public void updateNeighborVariableValuesNow(@Nullable Collection<IndexedValue> oldValue)
	{
		throw DimpleException.unsupportedMethod(getClass(), "updateNeighborVariableValuesNow");
	}

	@Override
	public void updateEdgeMessage(int portIndex)
	{
		//NOP
	}

	@Override
	public boolean setVisited(boolean visited)
	{
		boolean changed = _visited ^ visited;
		_visited = visited;
		return changed;
	}

}
