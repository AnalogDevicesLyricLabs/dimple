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

package com.analog.lyric.dimple.solvers.gibbs.sample;

// REFACTOR: move and rename
public class RealSample extends ObjectSample
{
	protected double _value;
	
	/*--------------
	 * Construction
	 */
	
	public RealSample(double value)
	{
		_value = value;
	}
	
	public RealSample(RealSample that)
	{
		this(that._value);
	}
	
	@Override
	public RealSample clone()
	{
		return new RealSample(this);
	}

	/*----------------------
	 * ObjectSample methods
	 */
	
	@Override
	public Object getObject()
	{
		return _value;
	}
	
	@Override
	public void setObject(Object value)
	{
		_value = ((Number)value).doubleValue();
	}
	
	public final double getValue() {return _value;}
	public final void setValue(double value) {_value = value;}
	
	@Override
	public double getDouble()
	{
		return _value;
	}
	
	@Override
	public void setDouble(double value)
	{
		_value = value;
	}
}
