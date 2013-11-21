package com.analog.lyric.dimple.factorfunctions.core;

import java.util.Iterator;

import net.jcip.annotations.NotThreadSafe;

/**
 * Iterator over entries in a {@link IFactorTable}.
 * <p>
 * Supports several styles of iteration. The first uses the traditional {@link Iterator} approach:
 * <pre>
 *     while (iter.hasNext())
 *     {
 *        FactorTableEntry entry = iter.next();
 *        ... entry.weight() ...
 *     }
 * </pre>
 * You can also iterate simply by calling {@link #next()} and testing for null:
 * <pre>
 *     for (FactorTableEntry entry; (entry = iter.next()) != null;)
 *     {
 *        ... entry.weight() ...
 *     }
 * </pre>
 * If you want to avoid allocating a new entry object for each iteration, you can instead use
 * {@link #advance()} and get the current attributes directly from the iterator:
 * <pre>
 *     while (iter.advance())
 *     {
 *        ... iter.weight() ...
 *     }
 * </pre>
 * 
 * Iterators can be obtained from {@link IFactorTable#iterator()} and {@link IFactorTable#fullIterator()}.
 * 
 * @since 0.05
 */
@NotThreadSafe
public interface IFactorTableIterator extends Iterator<FactorTableEntry>
{
	/*------------------
	 * Iterator methods
	 */
	
	/**
	 * Returns the next entry in the iteration or else null.
	 * It is not necessary to invoke {@link #hasNext()} before this method.
	 * 
	 * @see #advance()
	 */
	@Override
	public abstract FactorTableEntry next();
	
	/**
	 * Method not supported.
	 * @throws UnsupportedOperationException
	 */
	@Override
	public abstract void remove();

	/*------------------------------
	 * IFactorTableIterator methods
	 */

	/**
	 * Advance to the next entry.
	 * 
	 * @return false if this hit the end of iteration.
	 * @see #next()
	 */
	public abstract boolean advance();

	/**
	 * Returns current entry or null either if end of iteration has been reached or
	 * neither {@link #advance()} nor {@link #next()} has been called at least once.
	 */
	public abstract FactorTableEntry getEntry();

	/**
	 * Returns the energy for the entry at the current iteration.
	 * <p>
	 * 
	 */
	public abstract double energy();

	/**
	 * Returns the joint index for the entry at the current iteration or -1 if
	 * joint indices are not supported for this iterator.
	 */
	public abstract int jointIndex();

	/**
	 * Indicates whether the iterator will skip over table entries that have a zero weight
	 * (or infinite energy).
	 */
	public abstract boolean skipsZeroWeights();
	
	/**
	 * Returns the sparse index for the entry at the current iteration.
	 */
	public abstract int sparseIndex();

	/**
	 * Returns the weight for the entry at the current iteration.
	 */
	public abstract double weight();

}