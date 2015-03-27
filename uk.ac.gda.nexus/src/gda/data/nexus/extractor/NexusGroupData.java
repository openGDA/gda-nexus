/*-
 * Copyright © 2009-2013 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.data.nexus.extractor;

import gda.data.nexus.NexusGlobals;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data class that is returned by first class Nexus aware detectors
 */
public class NexusGroupData implements Serializable {
	transient private static final Logger logger = LoggerFactory.getLogger(NexusGroupData.class);		
	private Serializable data;
	
	/**
	 * dimensions - @see org.nexusformat.NexusFile.getInfo
	 */
	public int[] dimensions;

	/**
	 * This array may be used to indicate a preferred choice of chunking to the datawriter.
	 * The datawriter might well ignore that though (for now all will). 
	 */
	public int[] chunkDimensions = null;

	/**
	 * type of data e.g. NexusGlobals.NX_CHAR
	 */
	private final int type;

	/**
	 * Setting this can advise a datawriter to use the specified compression algorithm
	 * for a choice see:
	 * 
	 * NexusGlobals.NX_COMP_*
	 */
	public Integer compressionType = null;

	/**
	 * Flag to indicate that when writing this value to a file the entry is to linked to the NXEntry/NXDetector section as a variable of the scan
	 */
	public boolean isDetectorEntryData = false;

	private boolean isUnsigned = false;

	/**
	 * @param dimensions
	 * @param type
	 * @param data
	 */
	public NexusGroupData(int[] dimensions, int type, Serializable data) {
		super();
		this.dimensions = dimensions;
		this.type = type;
		this.data = data;
	}

	/**
	 * @param s String from which to make a NexusGroupData
	 */
	public NexusGroupData(String s) {
		super();
		try {
			data = s.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			data = s.getBytes();
		}
		dimensions = new int[1];
		dimensions[0] = s.length();
		type = NexusGlobals.NX_CHAR;
	}

	/**
	 * @param length
	 * @param s String from which to make a NexusGroupData
	 */
	public NexusGroupData(int length, String s) {
		super();
		try {
			data = Arrays.copyOf(s.getBytes("UTF-8"), length);
		} catch (UnsupportedEncodingException e) {
			data = Arrays.copyOf(s.getBytes(), length);
		}
		dimensions = new int[] {length};
		type = NexusGlobals.NX_CHAR;
	}

	public NexusGroupData(byte... b) {
		this(new int[]{b.length}, b);
	}

	public NexusGroupData(int[] dims, byte... b) {
		super();
		dimensions = dims;
		data = b;
		type = NexusGlobals.NX_INT8;
	}

	public NexusGroupData(short... s) {
		this(new int[]{s.length}, s);
	}

	public NexusGroupData(int[] dims, short... s) {
		super();
		dimensions = dims;
		data = s;
		type = NexusGlobals.NX_INT16;
	}

	public NexusGroupData(short[][] s) {
		super();
		dimensions = new int[] {s.length, s[0].length};
		data = s;
		type = NexusGlobals.NX_INT16;
	}

	public NexusGroupData(int... i) {
		this(new int[]{i.length}, i);
	}

	public NexusGroupData(int[] dims, int... i) {
		super();
		dimensions = dims;
		data = i;
		type = NexusGlobals.NX_INT32;
	}

	public NexusGroupData(int[][] i) {
		super();
		dimensions = new int[] {i.length, i[0].length};
		data = i;
		type = NexusGlobals.NX_INT32;
	}

	public NexusGroupData(int[][][] i) {
		super();
		dimensions = new int[] {i.length, i[0].length, i[0][0].length};
		data = i;
		type = NexusGlobals.NX_INT32;
	}

	public NexusGroupData(long... l) {
		this(new int[]{l.length}, l);
	}

	public NexusGroupData(int[] dims, long... l) {
		super();
		dimensions = dims;
		data = l;
		type = NexusGlobals.NX_INT64;
	}

	public NexusGroupData(boolean b) {
		this(b?1:0);
	}

	public NexusGroupData(float... f) {
		this(new int[]{f.length}, f);
	}

	public NexusGroupData(int[] dims, float... f) {
		super();
		dimensions = dims;
		data = f;
		type = NexusGlobals.NX_FLOAT32;
	}

	public NexusGroupData(double... d) {
		this(new int[]{d.length}, d);
	}

	public NexusGroupData(int[] dims, double... d) {
		super();
		dimensions = dims;
		data = d;
		type = NexusGlobals.NX_FLOAT64;
	}

	public NexusGroupData(double[][] d) {
		super();
		dimensions = new int[] {d.length, d[0].length};
		data = d;
		type = NexusGlobals.NX_FLOAT64;
	}

	public NexusGroupData(double[][][] d) {
		super();
		dimensions = new int[] {d.length, d[0].length, d[0][0].length};
		data = d;
		type = NexusGlobals.NX_FLOAT64;
	}

	/**
	 * Set type to be unsigned
	 * @return this
	 */
	public NexusGroupData setUnsigned() {
		switch (type) {
		case NexusGlobals.NX_INT8:
		case NexusGlobals.NX_INT16:
		case NexusGlobals.NX_INT32:
		case NexusGlobals.NX_INT64:
			isUnsigned = true;
			break;
		default:
			throw new UnsupportedOperationException("Can not set type to unsigned");
		}
		return this;
	}

	/**
	 * @return type of data e.g. NexusGlobals.NX_CHAR
	 */
	public int getType() {
		if (isUnsigned) {
			switch (type) {
			case NexusGlobals.NX_INT8:
				return NexusGlobals.NX_UINT8;
			case NexusGlobals.NX_INT16:
				return NexusGlobals.NX_UINT16;
			case NexusGlobals.NX_INT32:
				return NexusGlobals.NX_UINT32;
			case NexusGlobals.NX_INT64:
				return NexusGlobals.NX_UINT64;
			}
		}
		return type;
	}

	/**
	 * @return true if data contains characters
	 */
	public boolean isChar() {
		return getType() == NexusGlobals.NX_CHAR;
	}

	/**
	 * @return The data buffer compatible with type, null if data not extracted
	 */
	public Serializable getBuffer() {
		return data;
	}

	/**
	 * @return Returns the value of data and sets data to null
	 */
	public Object releaseData() {
		Object d = data;
		data = null;
		return d;
	}

	@Override
	public String toString() {
		StringBuffer msg = new StringBuffer("");
		msg.append("<dimensions>");
		for (int i : dimensions) {
			msg.append("<dimension>" + i + "</dimension>");
		}
		msg.append("</dimensions>");
		msg.append("<type>");
		switch (type) {
		case NexusGlobals.NX_CHAR:
			msg.append("NX_CHAR");
			break;
		case NexusGlobals.NX_FLOAT64:
			msg.append("NX_FLOAT64");
			break;
		default:
			msg.append(type);
			break;
		}
		msg.append("</type>");
		return msg.toString();
	}

	/**
	 * @param newlineAfterEach
	 * @param dataAsString
	 * @param wrap
	 * @return XML representation of the object
	 */
	public String dataToTxt(boolean newlineAfterEach, boolean dataAsString, boolean wrap) {
		StringBuffer msg = new StringBuffer();
		if (data != null) {
			if (type == NexusGlobals.NX_CHAR && data instanceof byte[]) {
				if (wrap)
					msg.append("<value>");
				msg.append(new String((byte[]) data));
				if (wrap)
					msg.append("</value>");
				if (newlineAfterEach) {
					msg.append("\n");
				}
			} else if (type == NexusGlobals.NX_CHAR && data instanceof String[]) {
				if (wrap)
					msg.append("<value>");
				String s = ((String[]) data)[0];
				msg.append(s);
				if (wrap)
					msg.append("</value>");
				if (newlineAfterEach) {
					msg.append("\n");
				}
			} else {
				if (dataAsString) {
					if (wrap)
						msg.append("<value>");
					if (data instanceof double[]) {
						double[] ddata = (double[]) (data);
						for (double d : ddata) {
							msg.append(Double.toString(d) + ",");
						}
						msg.deleteCharAt(msg.length()-1);
					} else if (data instanceof int[]) {
						int[] ddata = (int[]) (data);
						for (int d : ddata) {
							msg.append(Integer.toString(d) + ",");
						}
						msg.deleteCharAt(msg.length()-1);
					} else if (data instanceof byte[]) {
						byte[] ddata = (byte[]) (data);
						for (byte d : ddata) {
							msg.append(Byte.toString(d) + ",");
						}
						msg.deleteCharAt(msg.length()-1);
					} else if (data instanceof float[]) {
						float[] ddata = (float[]) (data);
						for (float d : ddata) {
							msg.append(Float.toString(d) + ",");
						}
						msg.deleteCharAt(msg.length()-1);
					} else if (data instanceof long[]) {
						long[] ddata = (long[]) (data);
						for (long d : ddata) {
							msg.append(Long.toString(d) + ",");
						}
						msg.deleteCharAt(msg.length()-1);
					} else {
						msg.append(data.toString());
					}
					if (wrap)
						msg.append("</value>");
					if (newlineAfterEach) {
						msg.append("\n");
					}
				} else {
					msg.append("<values>");
					if (newlineAfterEach) {
						msg.append("\n");
					}
					if (data instanceof double[]) {
						double[] ddata = (double[]) (data);
						for (double d : ddata) {
							msg.append("<value>");
							msg.append(Double.toString(d));
							msg.append("</value>");
							if (newlineAfterEach) {
								msg.append("\n");
							}
						}
					} else if (data instanceof int[]) {
						int[] ddata = (int[]) (data);
						for (int d : ddata) {
							msg.append("<value>");
							msg.append(Integer.toString(d));
							msg.append("</value>");
							if (newlineAfterEach) {
								msg.append("\n");
							}
						}
					} else if (data instanceof byte[]) {
						byte[] ddata = (byte[]) (data);
						for (byte d : ddata) {
							msg.append("<value>");
							msg.append(Byte.toString(d));
							msg.append("</value>");
							if (newlineAfterEach) {
								msg.append("\n");
							}
						}
					} else if (data instanceof float[]) {
						float[] ddata = (float[]) (data);
						for (float d : ddata) {
							msg.append("<value>");
							msg.append(Float.toString(d));
							msg.append("</value>");
							if (newlineAfterEach) {
								msg.append("\n");
							}
						}
					} else if (data instanceof long[]) {
						long[] ddata = (long[]) (data);
						for (long d : ddata) {
							msg.append("<value>");
							msg.append(Long.toString(d));
							msg.append("</value>");
							if (newlineAfterEach) {
								msg.append("\n");
							}
						}
					} else {
						msg.append(data.toString());
					}
					msg.append("</values>");
					if (newlineAfterEach) {
						msg.append("\n");
					}
				}
			}
		}
		return msg.toString();
	}

	public Serializable getFirstValue() {
		if (dimensions == null || data == null)
			return null;

		if (dimensions.length < 1)
			return null;

		if (dimensions[0] < 1)
			return null;

		Serializable value;

		// promote to int or double if possible
		switch (type) {
		case NexusGlobals.NX_CHAR:
			if (data instanceof String)
				value = data;
			else if (data instanceof String[])
				value = ((String [])data)[0];
			else
				value = new String((byte[]) data);
			break;
		case NexusGlobals.NX_FLOAT64:
			value = ((double[]) data)[0];
			break;
		case NexusGlobals.NX_FLOAT32:
			value = (double) ((float[]) data)[0];
			break;
		case NexusGlobals.NX_INT64:
			value = ((long[]) data)[0];
			break;
		case NexusGlobals.NX_INT32:
			value = ((int[]) data)[0];
			break;
		case NexusGlobals.NX_INT16:
			value = (int) ((short[]) data)[0];
			break;
		case NexusGlobals.NX_INT8:
			value = (int) ((byte[]) data)[0];
			break;
		default:
			value = null;
			break;
		}

		return value;
	}

	@Override
	public boolean equals(Object obj) {
		return equals(obj, false);
	}

	public boolean equals(Object obj, boolean logWhenFalseInOneCase) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		NexusGroupData other = (NexusGroupData) obj;
		if (data == null) {
			if (other.data != null) {
				return false;
			}
		} else {
			String dataToTxt = dataToTxt(false, false, false);
			String dataToTxt2 = other.dataToTxt(false, false, false);
			if (!dataToTxt.equals(dataToTxt2)) {
				if (logWhenFalseInOneCase)
					logger.info("expected = "+dataToTxt(false,true,false) + ", actual= " +other.dataToTxt(false,true,false));
				return false;
			}
		}
		return true;
	}

}
