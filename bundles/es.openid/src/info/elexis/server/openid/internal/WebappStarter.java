package info.elexis.server.openid.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.sql.DataSource;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jetty.osgi.boot.OSGiServerConstants;
import org.eclipse.jetty.webapp.WebAppContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

@Component(service = {})
public class WebappStarter {

	private WebAppContext webapp;

	private ServiceRegistration<?> serviceRegistration;

	@Reference(service = DataSource.class, cardinality = ReferenceCardinality.MANDATORY)
	protected synchronized void bindDataSource(DataSource dataSource) {
		// indicate data-source dependency to osgi
	}

	@Activate
	protected void activate() throws IOException {
		webapp = new WebAppContext();
		webapp.addBean(new JspStarter(webapp.getServletContext().getContextHandler()));
		webapp.setThrowUnavailableOnStartupException(true);

		Dictionary<String, String> props = new Hashtable<>();
		String warFile = FileLocator.toFileURL(Activator.getContext().getBundle().getEntry("lib/openid.war")).getPath();
		props.put("war", warFile);
		props.put("contextPath", "/openid");
		props.put(OSGiServerConstants.MANAGED_JETTY_SERVER_NAME, OSGiServerConstants.MANAGED_JETTY_SERVER_DEFAULT_NAME);
		serviceRegistration = Activator.getContext().registerService(WebAppContext.class.getName(), webapp, props);
	}

	@Deactivate
	protected void deactivate() {
		serviceRegistration.unregister();
	}

}
