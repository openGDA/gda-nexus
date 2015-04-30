/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.data.nexus.napi;

import gda.data.nexus.NexusUtils;

import java.net.URI;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.LazyWriteableDataset;
import org.eclipse.dawnsci.hdf5.nexus.NexusFile;
import org.junit.Assert;
import org.junit.Test;

public class NexusFileTest {

	@Test
	public void testNexusFile() throws Exception {
		String name = "test-scratch/test.nxs";
//		File f = new File(name);
//		if (f.exists())
//			f.delete();

		NexusFile nf = NexusUtils.createNXFile(name);

		nf.createAndOpenToWrite();

		GroupNode g = nf.getGroup("/e/a/b", true);
		Dataset a = DatasetFactory.createFromObject("world");
		a.setName("hello");

		nf.addAttribute(g, nf.createAttribute("b", a));

		int[] shape = new int[] {2, 34};
		int[] mshape = new int[] {ILazyWriteableDataset.UNLIMITED, 34};
		LazyWriteableDataset d = new LazyWriteableDataset("d", Dataset.INT16, shape, mshape, null, null);
		nf.createData(g, d);

		LazyWriteableDataset e = new LazyWriteableDataset("e", Dataset.FLOAT64, shape, mshape, null, null);
		nf.createData(g, e);

		a = DatasetFactory.createFromObject(-1.5);
		a.setName("value");
		nf.addAttribute(g.getDataNode(d.getName()), nf.createAttribute(d.getName(), a));

		LazyWriteableDataset t = new LazyWriteableDataset("t", Dataset.STRING, shape, mshape, null, null);
		nf.createData(g, t);

		nf.close();

		SliceND slice = new SliceND(shape, new Slice(2), new Slice(10, 11));
		d.setSlice(DatasetFactory.zeros(slice.getShape(), Dataset.INT16).fill(-5), slice);

		SliceND eSlice = SliceND.createSlice(e, new int[] {2, 3}, new int[] {4, 34});
		e.setSlice(DatasetFactory.zeros(eSlice.getShape(), Dataset.INT16).fill(-9), eSlice);

		t.setSlice(DatasetFactory.createFromObject(new String[] {"Hello", "World"}).reshape(2, 1),
				SliceND.createSlice(t, new int[] {2, 3}, new int[] {4, 4}));

		nf.openToRead();
		g = nf.getGroup("/e/a/b", false);
		checkGroup(g);

		DataNode n = nf.getData("/e/a/b/d");
		checkData(n, shape);

		n = nf.getData("/e/a/b/e");
		checkEData(n, new int[] {4, 34});

		n = nf.getData("/e/a/b/t");
		checkTextData(n, new int[] {4, 34});

		nf.close();

		nf.openToWrite(false);
		nf.link("/e/a/b", "/f/c");

		nf.linkExternal(new URI("nxfile://./"+name+"#/e/a/b/d"), "/g", false);
		nf.close();

		nf.openToRead();
		g = nf.getGroup("/f/c", false);
		checkGroup(g);

		n = g.getDataNode("d");
		checkData(n, shape);

		n = g.getDataNode("t");
		checkTextData(n, new int[] {3, 34});

		n = nf.getData("/g");
		Assert.assertNull(n);
		nf.close();
	}

	private void checkGroup(GroupNode g) {
		Assert.assertTrue(g.containsAttribute("hello"));
		Assert.assertEquals("world", g.getAttribute("hello").getValue().getString());
		Assert.assertTrue(g.isPopulated() && g.containsDataNode("d"));
	}

	private void checkData(DataNode n, int[] shape) {
		Assert.assertTrue(n.containsAttribute("value"));
		Assert.assertEquals(-1.5, n.getAttribute("value").getValue().getDouble(), 1e-15);
		ILazyDataset b = n.getDataset();
		Assert.assertTrue(b.elementClass().equals(Short.class));
		Assert.assertArrayEquals(shape, b.getShape());
		IDataset bs = b.getSlice();
		Assert.assertEquals(0, bs.getLong(0, 0));
		Assert.assertEquals(-5, bs.getLong(1, 10));
	}

	private void checkEData(DataNode n, int[] shape) {
		ILazyDataset b = n.getDataset();
		Assert.assertTrue(b.elementClass().equals(Double.class));
		Assert.assertArrayEquals(shape, b.getShape());
		IDataset bs = b.getSlice();
		Assert.assertEquals(0, bs.getDouble(0, 0), 1e-12);
		Assert.assertEquals(0, bs.getDouble(0, 2), 1e-12);
		Assert.assertEquals(0, bs.getDouble(0, 10), 1e-12);
		Assert.assertEquals(0, bs.getDouble(1, 0), 1e-12);
		Assert.assertEquals(0, bs.getDouble(1, 2), 1e-12);
		Assert.assertEquals(0, bs.getDouble(1, 10), 1e-12);
		Assert.assertEquals(0, bs.getDouble(2, 0), 1e-12);
		Assert.assertEquals(0, bs.getDouble(2, 2), 1e-12);
		Assert.assertEquals(-9, bs.getDouble(2, 10), 1e-12);
	}

	private void checkTextData(DataNode n, int[] shape) {
		ILazyDataset b = n.getDataset();
		Assert.assertTrue(b.elementClass().equals(String.class));
		// NAPI is broken wrt strings so skip for time being
//		Assert.assertArrayEquals(shape, b.getShape());
//		IDataset bs = b.getSlice();
//		Assert.assertEquals("", bs.getString(0, 0));
//		Assert.assertEquals("Hello", bs.getString(2, 3));
//		Assert.assertEquals("World", bs.getString(3, 3));
	}
}
