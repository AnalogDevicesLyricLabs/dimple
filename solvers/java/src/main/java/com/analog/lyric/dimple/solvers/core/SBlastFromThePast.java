/*******************************************************************************
*   Copyright 2013-2015 Analog Devices, Inc.
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

import static java.util.Objects.*;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.events.SolverEventSource;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.repeated.BlastFromThePastFactor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.interfaces.ISolverBlastFromThePastFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.util.misc.Internal;

public class SBlastFromThePast extends SolverEventSource implements ISolverBlastFromThePastFactor
{
	private BlastFromThePastFactor _factor;
	protected final Port _portForOtherVar;
	protected final Port _portForBlastVar;
	protected @Nullable ISolverFactorGraph _parent = null;
	
	public SBlastFromThePast(BlastFromThePastFactor f)
	{
		_factor = f;
		_portForOtherVar = f.getPortForOtherVariable();
		Variable varConnectedToBlast = f.getVariableConnectedToBlast();
	    _portForBlastVar = new Port(varConnectedToBlast,varConnectedToBlast.getPortNum(getModelObject()));
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public String toString()
	{
		return String.format("[%s %s]", getClass().getSimpleName(), _factor.getQualifiedName());
	}
	
	/*----------------------
	 * ISolverNode methods
	 */
	
	@Override
	public ISolverVariable getSibling(int edge)
	{
		// FIXME - lookup directly in solver graph
		return Objects.requireNonNull(getModelObject().getSibling(edge).getSolver());
	}
	
	@Override
	public int getSiblingCount()
	{
		return getModelObject().getSiblingCount();
	}
	
	/*---------------------------
	 * SBlastFromThePast methods
	 */
	
	public BlastFromThePastFactor getFactor()
	{
		return _factor;
	}
	
	public @Nullable Port getOtherVariablePort()
	{
		return _portForOtherVar;
	}
	
	@Override
	public Object getBelief()
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public double getInternalEnergy()
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public double getBetheEntropy()
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public void createMessages()
	{
		throw new DimpleException("not supported");
	}

	@Override
	public void createMessages(Variable varConnectedToBlast, Port portForOtherVar)
	{
		final Factor factor = _factor;
	    for (int index = 0, nVars = factor.getSiblingCount(); index < nVars; index++)
	    {
	    	ISolverVariable is = requireNonNull(factor.getSibling(index).getSolver());
	    	is.createMessages(this);
	    }
	    
	    assert(_portForOtherVar == portForOtherVar);
	    assert(_portForBlastVar.node == varConnectedToBlast);
	}

	@Override
	public void moveMessages(ISolverNode other)
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public int[][] getPossibleBeliefIndices()
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public void update()
	{
	}

	@Override
	public void updateEdge(int outPortNum)
	{
	}

	@Override
	public void initialize()
	{
		clearFlags();
	}

	@Override
	public void resetEdgeMessages(int portNum)
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public @Nullable ISolverFactorGraph getParentGraph()
	{
		return _parent;
	}

	@Override
	public @Nullable ISolverFactorGraph getRootSolverGraph()
	{
		final ISolverFactorGraph parent = _parent;
		return parent != null ? parent.getRootSolverGraph() : null;
	}

	@Override
	public double getScore()
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public Factor getModelObject()
	{
		return _factor;
	}

	@Override
	public Object getInputMsg(int portIndex)
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public Object getOutputMsg(int portIndex)
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public void setInputMsg(int portIndex, Object obj)
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public void setOutputMsg(int portIndex, Object obj)
	{
		Variable var = _factor.getSibling(portIndex);
		int index = _factor.getSiblingPortIndex(portIndex);
		// FIXME - lookup through solver graph
		var.requireSolver("setOutputMsg").setInputMsg(index,obj);
	}

	@Override
	public void setInputMsgValues(int portIndex, Object obj)
	{
		throw new DimpleException("Not implemented");
	}

	@Override
	public void setOutputMsgValues(int portIndex, Object obj)
	{
		throw new DimpleException("Not implemented");
	}

	@Internal
	@Override
	public void setParent(ISolverFactorGraph parent)
	{
		_parent = parent;
	}
	
	@Override
	public void moveMessages(ISolverNode other, int thisPortNum,
			int otherPortNum)
	{
		throw new DimpleException("Not implemented");
	}

	@SuppressWarnings("null")
	@Override
	public void advance()
	{
		// FIXME lookup through solver graph
		_portForBlastVar.node.getSolver().moveMessages(_portForOtherVar.node.getSolver(),
				_portForBlastVar.index,_portForOtherVar.index);
	}

	@Override
	public void setDirectedTo(int[] indices)
	{
		//NOP
	}

	

}
