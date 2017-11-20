package info.elexis.server.core.connector.elexis.billable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.service.event.Event;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.status.ObjectStatus;
import info.elexis.server.core.connector.elexis.jpa.ElexisTypeMap;
import info.elexis.server.core.connector.elexis.jpa.StoreToStringService;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Artikel;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Labor2009Tarif;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.PhysioLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Stock;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.StockEntry;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
import info.elexis.server.core.connector.elexis.jpa.test.eventHandler.TestEventHandler;
import info.elexis.server.core.connector.elexis.services.AbstractServiceTest;
import info.elexis.server.core.connector.elexis.services.ArtikelService;
import info.elexis.server.core.connector.elexis.services.ArtikelstammItemService;
import info.elexis.server.core.connector.elexis.services.JPAQuery;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;
import info.elexis.server.core.connector.elexis.services.Labor2009TarifService;
import info.elexis.server.core.connector.elexis.services.PersistenceService;
import info.elexis.server.core.connector.elexis.services.PhysioLeistungService;
import info.elexis.server.core.connector.elexis.services.StockEntryService;
import info.elexis.server.core.connector.elexis.services.StockService;
import info.elexis.server.core.connector.elexis.services.VerrechnetService;

public class BillingTest extends AbstractServiceTest {

	private static Kontakt userContact;
	private static Kontakt mandator;
	private Verrechnet vr;

	@BeforeClass
	public static void init() {
		JPAQuery<Kontakt> mandantQuery = new JPAQuery<Kontakt>(Kontakt.class);
		mandantQuery.add(Kontakt_.person, QUERY.EQUALS, true);
		mandantQuery.add(Kontakt_.mandator, QUERY.EQUALS, true);
		List<Kontakt> mandants = mandantQuery.execute();
		assertTrue(!mandants.isEmpty());
		mandator = mandants.get(0);
		userContact = mandator;

		PersistenceService.setThreadLocalUserId("testUserId");
	}

	@Before
	public void before() {
		createTestMandantPatientFallBehandlung();
		createTestMandantPatientFallBehandlung();
		createTestMandantPatientFallBehandlung();
		createTestMandantPatientFallBehandlung();
		createTestMandantPatientFallBehandlung();
		createTestMandantPatientFallBehandlung();
	}

	@After
	public void teardownPatientAndBehandlung() {
		cleanup();
	}

	@Test
	public void testAddLaborTarif2009Billing() {
		Labor2009Tarif immunglobulinValid = Labor2009TarifService.load("a6e58fc71c723bd54016760").get();
		assertNotNull(immunglobulinValid);
		VerrechenbarLabor2009Tarif validLabTarif = new VerrechenbarLabor2009Tarif(immunglobulinValid);

		IStatus status = validLabTarif.add(testBehandlungen.get(0), userContact, mandator);
		Event event = TestEventHandler.waitforEvent();
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);
		TestEventHandler.assertCreateEvent(event, ElexisTypeMap.TYPE_VERRECHNET);
		assertEquals(vr.getId(), event.getProperty(ElexisEventTopics.PROPKEY_ID));

		assertEquals(immunglobulinValid.getName(), vr.getLeistungenText());
		assertEquals(12000, vr.getVk_tp());
		assertEquals(12000, vr.getVk_preis());
		assertEquals(100, vr.getScale());
		assertEquals(100, vr.getScale2());
		assertEquals(immunglobulinValid.getId(), vr.getLeistungenCode());
		assertEquals(testBehandlungen.get(0).getId(), vr.getBehandlung().getId());
		assertEquals(ElexisTypeMap.TYPE_LABOR2009TARIF, vr.getKlasse());
		assertEquals(1, vr.getZahl());

		Labor2009Tarif immunglobulinInvalid = Labor2009TarifService.load("ub49a50af4d3e51e40906").get();
		VerrechenbarLabor2009Tarif invalidLabTarif = new VerrechenbarLabor2009Tarif(immunglobulinInvalid);

		status = invalidLabTarif.add(testBehandlungen.get(0), userContact, mandator);
		assertTrue(status.getMessage(), !status.isOK());
	}

	@Test
	public void testAddLaborTarif2009BillingFindByCode() {
		Labor2009Tarif immunglobulinValid = Labor2009TarifService.findFromCode("1442.00").get();
		assertNotNull(immunglobulinValid);
		VerrechenbarLabor2009Tarif validLabTarif = new VerrechenbarLabor2009Tarif(immunglobulinValid);
		IStatus status = validLabTarif.add(testBehandlungen.get(0), userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);
	}

	@Test
	public void testAddPhysioLeistungBilling() {
		PhysioLeistung validDefault = PhysioLeistungService.findFromCode("7301").get();
		assertNotNull(validDefault);
		VerrechenbarPhysioLeistung validPhysTarif = new VerrechenbarPhysioLeistung(validDefault);
		IStatus status = validPhysTarif.add(testBehandlungen.get(0), userContact, mandator);
		Event event = TestEventHandler.waitforEvent();
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);
		TestEventHandler.assertCreateEvent(event, ElexisTypeMap.TYPE_VERRECHNET);
		assertEquals(vr.getId(), event.getProperty(ElexisEventTopics.PROPKEY_ID));

		assertEquals(validDefault.getTitel(), vr.getLeistungenText());
		assertEquals("0.89", vr.getVk_scale());
		assertEquals(4800, vr.getVk_tp());
		assertEquals(4272, vr.getVk_preis());
		assertEquals(100, vr.getScale());
		assertEquals(100, vr.getScale2());
		assertEquals(validDefault.getId(), vr.getLeistungenCode());
		assertEquals(testBehandlungen.get(0).getId(), vr.getBehandlung().getId());
		assertEquals(ElexisTypeMap.TYPE_PHYSIOLEISTUNG, vr.getKlasse());
		assertEquals(1, vr.getZahl());
	}

	@Test
	public void testAddArtikelstammBilling() {
		Optional<ArtikelstammItem> artikelstammItem = ArtikelstammItemService.findByGTIN("7680531600264");
		assertTrue(artikelstammItem.isPresent());

		ArtikelstammItemService.save(artikelstammItem.get());

		VerrechenbarArtikelstammItem verrechenbar = new VerrechenbarArtikelstammItem(artikelstammItem.get());

		IStatus status = verrechenbar.add(testBehandlungen.get(0), userContact, mandator);
		Event event = TestEventHandler.waitforEvent();
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);
		TestEventHandler.assertCreateEvent(event, ElexisTypeMap.TYPE_VERRECHNET);
		assertEquals(vr.getId(), event.getProperty(ElexisEventTopics.PROPKEY_ID));

		assertEquals(ElexisTypeMap.TYPE_ARTIKELSTAMM, vr.getKlasse());
		assertEquals(artikelstammItem.get().getDscr(), vr.getLeistungenText());
		assertEquals(testBehandlungen.get(0).getId(), vr.getBehandlung().getId());
		assertEquals(1, vr.getZahl());
		assertEquals("1.0", vr.getVk_scale());
		Double ppub = Double.valueOf(artikelstammItem.get().getPpub()) * 100;
		assertEquals(ppub.intValue(), vr.getVk_tp());
		assertEquals(ppub.intValue(), vr.getVk_preis());
		assertEquals(100, vr.getScale());
		assertEquals(100, vr.getScale2());

		artikelstammItem = ArtikelstammItemService.findByGTIN("7680531600264");
		assertTrue(artikelstammItem.isPresent());
	}

	@Test
	public void testAddEigenartikelBilling() {
		Artikel ea1 = new ArtikelService.Builder("NameVerrechnen", "InternalName", Artikel.TYP_EIGENARTIKEL).build();
		ea1.setEkPreis("150");
		ea1.setVkPreis("-300"); // emulate user-defined price
		ArtikelService.save(ea1);

		VerrechenbarArtikel verrechenbar = new VerrechenbarArtikel(ea1);

		IStatus status = verrechenbar.add(testBehandlungen.get(0), userContact, mandator);
		Event event = TestEventHandler.waitforEvent();
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);
		assertTrue(os.isOK());
		TestEventHandler.assertCreateEvent(event, ElexisTypeMap.TYPE_VERRECHNET);
		assertEquals(vr.getId(), event.getProperty(ElexisEventTopics.PROPKEY_ID));

		assertEquals(ElexisTypeMap.TYPE_EIGENARTIKEL, vr.getKlasse());
		assertEquals(ea1.getLabel(), vr.getLeistungenText());
		assertEquals(testBehandlungen.get(0).getId(), vr.getBehandlung().getId());
		assertEquals(1, vr.getZahl());
		assertEquals(300, vr.getVk_preis());
		assertEquals(100, vr.getScale());
		assertEquals(100, vr.getScale2());

		ArtikelService.remove(ea1);
	}

	@Test
	public void testChangeCountAddOnVerrechnetValid() {
		Optional<ArtikelstammItem> artikelstammItem = ArtikelstammItemService.findByGTIN("7680531600264");
		VerrechenbarArtikelstammItem verrechenbar = new VerrechenbarArtikelstammItem(artikelstammItem.get());

		IStatus status = verrechenbar.add(testBehandlungen.get(1), userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);

		IStatus valid = VerrechnetService.changeCountValidated(vr, 3, mandator);
		assertTrue(valid.isOK());
		vr = (Verrechnet) ((ObjectStatus) valid).getObject();
		assertEquals(3, vr.getZahl());
		assertEquals(100, vr.getScale());
		assertEquals(100, vr.getScale2());

		List<Verrechnet> allVerrechnet = VerrechnetService.getAllVerrechnetForBehandlung(vr.getBehandlung());
		assertEquals(1, allVerrechnet.size());
		assertEquals(3, allVerrechnet.get(0).getZahl());
		Double ppub = Double.valueOf(artikelstammItem.get().getPpub()) * 100;
		assertEquals(ppub.intValue(), vr.getVk_tp());
		assertEquals(ppub.intValue(), vr.getVk_preis());
	}

	@Test
	public void testChangeCountRemoveOnVerrechnetValid() {
		Optional<ArtikelstammItem> artikelstammItem = ArtikelstammItemService.findByGTIN("7680531600264");
		VerrechenbarArtikelstammItem verrechenbar = new VerrechenbarArtikelstammItem(artikelstammItem.get());

		IStatus status = verrechenbar.add(testBehandlungen.get(1), userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);

		IStatus valid = VerrechnetService.changeCountValidated(vr, 3, mandator);
		assertTrue(valid.isOK());

		List<Verrechnet> allVerrechnet = VerrechnetService.getAllVerrechnetForBehandlung(testBehandlungen.get(1));
		assertEquals(vr.getId(), allVerrechnet.get(0).getId());
		assertEquals(1, allVerrechnet.size());
		assertEquals(3, allVerrechnet.get(0).getZahl());
		Double ppub = Double.valueOf(artikelstammItem.get().getPpub()) * 100;
		assertEquals(ppub.intValue(), vr.getVk_tp());
		assertEquals(ppub.intValue(), vr.getVk_preis());

		allVerrechnet = VerrechnetService.getAllVerrechnetForBehandlung(testBehandlungen.get(1));
		valid = VerrechnetService.changeCountValidated(allVerrechnet.get(0), 1, mandator);
		assertTrue(valid.isOK());

		assertEquals(1, allVerrechnet.size());
		assertEquals(1, allVerrechnet.get(0).getZahl());
	}

	@Test
	public void testChargeArtikelstammBillableWithHigherOneIntegerCount() {
		Optional<ArtikelstammItem> artikelstammItem = ArtikelstammItemService.findByGTIN("7680531600264");
		VerrechenbarArtikelstammItem verrechenbar = new VerrechenbarArtikelstammItem(artikelstammItem.get());

		IStatus status = verrechenbar.add(testBehandlungen.get(3), userContact, mandator, 5);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);
		assertEquals(5, vr.getZahl());
		assertEquals(100, vr.getScale());
		assertEquals(100, vr.getScale2());
	}

	@Test
	public void testChargeArtikelstammBillableWithLowerOneFloatCount() {
		Optional<ArtikelstammItem> artikelstammItem = ArtikelstammItemService.findByGTIN("7680531600264");
		VerrechenbarArtikelstammItem verrechenbar = new VerrechenbarArtikelstammItem(artikelstammItem.get());

		IStatus status = verrechenbar.add(testBehandlungen.get(3), userContact, mandator, 0.5f);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);
		assertEquals(1, vr.getZahl());
		assertEquals(100, vr.getScale());
		assertEquals(50, vr.getScale2());
		
		status = verrechenbar.add(testBehandlungen.get(3), userContact, mandator, 0.3f);
		assertTrue(status.getMessage(), status.isOK());
		os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);
		assertEquals(1, vr.getZahl());
		assertEquals(100, vr.getScale());
		assertEquals(80, vr.getScale2());
	}

	@Test
	public void testStockRemovalOnArticleDisposal() {
		Artikel ea1 = new ArtikelService.Builder("NameVerrechnen", "InternalName", Artikel.TYP_EIGENARTIKEL).build();
		ea1.setEkPreis("150");
		ea1.setVkPreis("300");
		ArtikelService.save(ea1);

		StockService stockService = new StockService();
		Stock defaultStock = StockService.load("STD").get();
		IStockEntry se = stockService.storeArticleInStock(defaultStock, StoreToStringService.storeToString(ea1));
		se.setMinimumStock(5);
		se.setCurrentStock(10);
		se.setMaximumStock(15);
		StockEntryService.save((StockEntry) se);

		VerrechenbarArtikel verrechenbar = new VerrechenbarArtikel(ea1);

		IStatus status = verrechenbar.add(testBehandlungen.get(0), userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);
		assertTrue(os.isOK());

		assertEquals(ElexisTypeMap.TYPE_EIGENARTIKEL, vr.getKlasse());
		assertEquals(ea1.getLabel(), vr.getLeistungenText());
		assertEquals(testBehandlungen.get(0).getId(), vr.getBehandlung().getId());
		assertEquals(1, vr.getZahl());
		assertEquals(300, vr.getVk_preis());
		assertEquals(100, vr.getScale());

		Integer stockValue = stockService.getCumulatedStockForArticle(ea1);
		assertEquals(9, stockValue.intValue());

		ArtikelService.remove(ea1);
	}

	@Test
	public void testChangeCountToFractionalOnVerrechnetValid() {
		Optional<ArtikelstammItem> artikelstammItem = ArtikelstammItemService.findByGTIN("7680531600264");
		VerrechenbarArtikelstammItem verrechenbar = new VerrechenbarArtikelstammItem(artikelstammItem.get());

		IStatus status = verrechenbar.add(testBehandlungen.get(5), userContact, mandator);
		assertTrue(status.getMessage(), status.isOK());
		ObjectStatus os = (ObjectStatus) status;
		vr = (Verrechnet) os.getObject();
		assertNotNull(vr);

		IStatus valid = VerrechnetService.changeCountValidated(vr, 0.2f, mandator);
		vr = (Verrechnet) ((ObjectStatus) valid).getObject();
		assertTrue(valid.isOK());
		assertEquals(1, vr.getZahl());
		assertEquals(100, vr.getScale());
		assertEquals(20, vr.getScale2());

		valid = VerrechnetService.changeCountValidated(vr, 2, mandator);
		vr = (Verrechnet) ((ObjectStatus) valid).getObject();
		assertTrue(valid.isOK());
		assertEquals(2, vr.getZahl());
		assertEquals(100, vr.getScale());
		assertEquals(100, vr.getScale2());

		valid = VerrechnetService.changeCountValidated(vr, 1.6f, mandator);
		vr = (Verrechnet) ((ObjectStatus) valid).getObject();
		assertTrue(valid.isOK());
		assertEquals(1, vr.getZahl());
		assertEquals(100, vr.getScale());
		assertEquals(160, vr.getScale2());

		valid = VerrechnetService.changeCountValidated(vr, 1, mandator);
		vr = (Verrechnet) ((ObjectStatus) valid).getObject();
		assertTrue(valid.isOK());
		assertEquals(1, vr.getZahl());
		assertEquals(100, vr.getScale());
		assertEquals(100, vr.getScale2());

		valid = VerrechnetService.changeCountValidated(vr, 3, mandator);
		vr = (Verrechnet) ((ObjectStatus) valid).getObject();
		assertTrue(valid.isOK());
		assertEquals(3, vr.getZahl());
		assertEquals(100, vr.getScale());
		assertEquals(100, vr.getScale2());

		valid = VerrechnetService.changeCountValidated(vr, 0.5f, mandator);
		vr = (Verrechnet) ((ObjectStatus) valid).getObject();
		assertTrue(valid.isOK());
		assertEquals(1, vr.getZahl());
		assertEquals(100, vr.getScale());
		assertEquals(50, vr.getScale2());
	}


}
