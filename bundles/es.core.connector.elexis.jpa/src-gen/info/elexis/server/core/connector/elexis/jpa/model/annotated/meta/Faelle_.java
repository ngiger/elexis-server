package info.elexis.server.core.connector.elexis.jpa.model.annotated.meta;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Faelle;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import java.time.LocalDate;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.3.2.v20111125-r10461", date="2016-01-28T08:14:59")
@StaticMetamodel(Faelle.class)
public class Faelle_ { 

    public static volatile SingularAttribute<Faelle, LocalDate> datumVon;
    public static volatile SingularAttribute<Faelle, String> fallNummer;
    public static volatile SingularAttribute<Faelle, String> grund;
    public static volatile SingularAttribute<Faelle, String> bezeichnung;
    public static volatile SingularAttribute<Faelle, String> diagnosen;
    public static volatile SingularAttribute<Faelle, String> betriebsNummer;
    public static volatile SingularAttribute<Faelle, String> versNummer;
    public static volatile SingularAttribute<Faelle, byte[]> extInfo;
    public static volatile SingularAttribute<Faelle, Kontakt> garantKontakt;
    public static volatile SingularAttribute<Faelle, Kontakt> kostentrKontakt;
    public static volatile SingularAttribute<Faelle, LocalDate> datumBis;
    public static volatile SingularAttribute<Faelle, String> gesetz;
    public static volatile SingularAttribute<Faelle, Kontakt> patientKontakt;
    public static volatile SingularAttribute<Faelle, String> status;

}