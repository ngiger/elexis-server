package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription;
import info.elexis.server.core.connector.elexis.jpa.test.TestEntities;

public class PrescriptionServiceTest extends AbstractServiceTest {

	private ArtikelstammItem article;
	private ArtikelstammItem product;

	@Before
	public void before() {
		createTestMandantPatientFallBehandlung();

		article = ArtikelstammItemService.INSTANCE.findById(TestEntities.ARTIKELSTAMM_ITEM_PHARMA_ID).get();
		product = ArtikelstammItemService.INSTANCE.findById(TestEntities.ARTIKELSTAMM_PRODUCT_PHARMA_ID).get();
		assertNotNull(article);
		assertNotNull(product);
	}

	@After
	public void after() {
		ArtikelstammItemService.INSTANCE.remove(article);
		ArtikelstammItemService.INSTANCE.remove(product);
	}

	@Test
	public void testAddAndRemovePrescription() {
		Prescription articlePres = PrescriptionService.INSTANCE.create(article, testPatients.get(0), "1-1-0-0");
		Prescription productPres = PrescriptionService.INSTANCE.create(product, testPatients.get(0), "1-1-0-0");
		Prescription deletedPres = PrescriptionService.INSTANCE.create(article, testPatients.get(0), "1-1-2-1");
		deletedPres.setDeleted(true);
		Prescription recipePres = PrescriptionService.INSTANCE.create(article, testPatients.get(0), "1-1-2-1");
		recipePres.setRezeptID("nonExistRecipeId");
		PrescriptionService.INSTANCE.flush();

		assertNotNull(articlePres.getDateFrom());
		assertEquals("1-1-0-0", articlePres.getDosis());
		assertNotNull(productPres.getDateFrom());

		List<Prescription> prescList = PrescriptionService
				.findAllNonDeletedPrescriptionsForPatient(testPatients.get(0));
		assertEquals(2, prescList.size());

		PrescriptionService.INSTANCE.remove(articlePres);
		PrescriptionService.INSTANCE.remove(productPres);
		PrescriptionService.INSTANCE.remove(deletedPres);
		PrescriptionService.INSTANCE.remove(recipePres);
	}

}
