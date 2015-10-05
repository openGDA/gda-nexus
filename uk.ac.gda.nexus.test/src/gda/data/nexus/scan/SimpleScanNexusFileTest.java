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

package gda.data.nexus.scan;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import gda.data.nexus.NexusUtils;
import gda.util.TestUtils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.StringDataset;
import org.eclipse.dawnsci.nexus.NXbeam;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NXsample;
import org.eclipse.dawnsci.nexus.impl.NXbeamImpl;
import org.eclipse.dawnsci.nexus.impl.NXdetectorImpl;
import org.eclipse.dawnsci.nexus.impl.NXobjectFactory;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimpleScanNexusFileTest {

	public class TestDetector implements NexusDevice<NXdetector> {

		@Override
		public Class<NXdetector> getNexusBaseClass() {
			return NXdetector.class;
		}

		@Override
		public NXdetector createBaseClassInstance(NXobjectFactory nxObjectFactory) {
			final NXdetectorImpl nxDetector = nxObjectFactory.createNXdetector();

			nxDetector.setDescription(StringDataset.createFromObject("Test Detector"));
			// TODO set more fields

			return nxDetector;
		}

		@Override
		public DeviceType getDeviceType() {
			return DeviceType.INSTRUMENT;
		}

		@Override
		public String getName() {
			return "analyser";
		}

		@Override
		public ILazyWriteableDataset getDataset() {
			return null;
		}

	}

	public class TestBeam implements NexusDevice<NXbeam> {

		@Override
		public Class<NXbeam> getNexusBaseClass() {
			return NXbeam.class;
		}

		@Override
		public NXbeam createBaseClassInstance(NXobjectFactory nxObjectFactory) {
			final NXbeamImpl beam = nxObjectFactory.createNXbeam();
			beam.setIncident_wavelength(DatasetFactory.createFromObject(123.456));
			beam.setFlux(DatasetFactory.createFromObject(12.34f));

			return beam;
		}

		@Override
		public gda.data.nexus.scan.NexusDevice.DeviceType getDeviceType() {
			return DeviceType.SAMPLE;
		}

		@Override
		public String getName() {
			return "beam";
		}

		@Override
		public ILazyWriteableDataset getDataset() {
			return null;
		}

	}

	private static final String FILE_NAME = "nexusTestFile.nx5";

	private static String testScratchDirectoryName;

	private static String filePath;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(SimpleScanNexusFileTest.class.getCanonicalName());
		TestUtils.makeScratchDirectory(testScratchDirectoryName);
		filePath = testScratchDirectoryName + FILE_NAME;
	}

	@Test
	public void testNexusScan() throws Exception {
		// setup the scan
		final List<NexusDevice<?>> devices = new ArrayList<>();
		devices.add(new TestDetector());
		devices.add(new TestBeam());

		// setup and create the nexus file builder
		final DefaultNexusFileBuilder nexusFileBuilder = new DefaultNexusFileBuilder();
		nexusFileBuilder.setNexusDevices(devices);
		nexusFileBuilder.setFilePath(filePath);
		nexusFileBuilder.buildNexusFile();

		// check the constructed tree is as expected
		final TreeFile nexusTree = nexusFileBuilder.getNexusTree();
		checkNexusTree(nexusTree);

		// save the nexus file
		NexusUtils.saveNexusFile(nexusTree);

		// load the saved file and check the loaded tree is as expected
		TreeFile reloadedNexusTree = NexusUtils.loadNexusFile(filePath, true);
		checkNexusTree(reloadedNexusTree);
	}

	public void checkNexusTree(TreeFile nexusTree) {
		final NXroot rootNode = (NXroot) nexusTree.getGroupNode();
		assertNotNull(rootNode);
		assertEquals(1, rootNode.getNumberOfGroupNodes());
		assertEquals(0, rootNode.getNumberOfDataNodes());

		NXentry entry = rootNode.getEntry();
		assertNotNull(entry);
		assertEquals(3, entry.getNumberOfGroupNodes());
		assertEquals(0, rootNode.getNumberOfDataNodes());

		NXinstrument instrument = entry.getInstrument();
		checkInstrument(instrument);

		NXsample sample = entry.getSample();
		checkSample(sample);
	}

	private void checkInstrument(NXinstrument instrument) {
		assertNotNull(instrument);
		assertEquals(1, instrument.getNumberOfGroupNodes());
		assertEquals(0, instrument.getNumberOfDataNodes());

		NXdetector detector = instrument.getDetector("analyser");
		assertNotNull(detector);
		assertEquals("Test Detector", detector.getDescription().getString());
	}

	private void checkSample(NXsample sample) {
		assertNotNull(sample);
		assertEquals(1, sample.getNumberOfGroupNodes());
		assertEquals(0, sample.getNumberOfDataNodes());

		NXbeam beam = sample.getBeam();
		assertNotNull(beam);
		assertEquals(123.456, beam.getIncident_wavelength().getDouble(), 1e-15);
		assertEquals(12.34f, beam.getFlux().getFloat(), 1e-7);
	}

}
