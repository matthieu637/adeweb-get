import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;

public class Outils {

	public static String chercherLigne(String full, String motif) throws IOException{
		BufferedReader br = new BufferedReader(new StringReader(full));
		
		StringBuffer sb = new StringBuffer();
		String ligne = null;
		while ((ligne = br.readLine()) != null)
			if (ligne.indexOf(motif) >= 0)
				sb.append(ligne).append("\n");

		return new String(sb);
	}
	
	
	
	public static String istreamToString(InputStream s) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(s);
		InputStreamReader isr = new InputStreamReader(bis);
		BufferedReader br = new BufferedReader(isr);

		StringBuffer sb = new StringBuffer();
		String ligne = null;
		while ((ligne = br.readLine()) != null)
			sb.append(ligne).append("\n");

		return new String(sb);
	}

	public static String istreamToStringFind(InputStream s, String contient)
			throws IOException {
		BufferedInputStream bis = new BufferedInputStream(s);
		InputStreamReader isr = new InputStreamReader(bis);
		BufferedReader br = new BufferedReader(isr);

		StringBuffer sb = new StringBuffer();
		String ligne = null;
		while ((ligne = br.readLine()) != null)
			if (ligne.indexOf(contient) >= 0)
				sb.append(ligne).append("\n");

		return new String(sb);
	}

	public static DefaultHttpClient wrapClient(HttpClient base) {
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {

				@Override
				public void checkClientTrusted(
						java.security.cert.X509Certificate[] chain,
						String authType) throws CertificateException {
					// TODO Auto-generated method stub

				}

				@Override
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] chain,
						String authType) throws CertificateException {
					// TODO Auto-generated method stub

				}

				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					// TODO Auto-generated method stub
					return null;
				}
			};
			ctx.init(null, new TrustManager[] { tm }, null);
			SSLSocketFactory ssf = new SSLSocketFactory(ctx);
			ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = base.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", ssf, 443));
			return new DefaultHttpClient(ccm, base.getParams());
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
