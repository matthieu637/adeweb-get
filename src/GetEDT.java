import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * @author matthieu637
 * 
 */
public class GetEDT{

	private DefaultHttpClient	httpclient;
	private String				It;
	private String				id;
	private String				tiquetADE;

	public static void main(String[] args) throws ClientProtocolException, IOException {
		String message = "Usage : GetEDT <login> <password> <edt|ical> <id_annee> <recherche> <id_cal> <chemin> <...> \n"
			+ "si edt : <semaine> <largeur> <longueur> \n" + "si ical : <jour> <mois> <annee> (jusqu'à) <jour> <mois> <annee>";

		if(args == null || (args.length != 10 && args.length != 13) || (!args[2].equalsIgnoreCase("edt") && !args[2].equalsIgnoreCase("ical"))) {
			System.out.println(message);
		} else {
			GetEDT edt = new GetEDT(args[0], args[1]);

			if(args.length == 10 && args[2].equalsIgnoreCase("edt"))
				edt.enregistrerEDT(args[3], args[4], args[5], args[6], args[7], args[8], args[9]);
			else if(args.length == 13 && args[2].equalsIgnoreCase("ical"))
				edt.enregistrerICal(args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12]);
			else
				System.out.println("2\n" + message);
		}
	}

	public static void test() throws ClientProtocolException, IOException {
		GetEDT edt = new GetEDT("zimme0511", "e`J5b]Y9");
		edt.enregistrerEDT("13", "S5 INFO TP1", "18300", "testS5", "30", "800", "600");

		edt = new GetEDT("zimme0511", "e`J5b]Y9");
		edt.enregistrerEDT("1", "S6 INFO", "18299", "testS6", "1", "800", "600");

		// edt = new GetEDT("zimme0511", "e`J5b]Y9");
		// edt.enregistrerICal("1", "S6 INFO", "18299", "ical", "05", "09", "2011");
	}

	public GetEDT(String login, String password) throws ClientProtocolException, IOException {
		creerClientHTTPS();
		recupererClesUHP();
		identificationUHP(login, password);
	}

	public void enregistrerEDT(String id_annee, String recherche, String id_EDT, String chemin, String semaine, String width, String height)
		throws ClientProtocolException, IOException {
		recupereTiquetADE();
		choisirAnneeADE(id_annee);
		rechercheADE(recherche);

		int no_semaine = Integer.parseInt(semaine);
		String semaine_str, semaine_str2;

		if(no_semaine < 22) // semestre automne
			for(int i = no_semaine; i < 22; i += 2) {
				semaine_str = String.valueOf(i);
				semaine_str2 = String.valueOf(i + 1);
				choixEDTParmiReponsesADE(id_EDT);
				choixDesignParDefault();

				String params[] = creerLienVersImage();
				String lienImage = personnalisationLienSansWeekEndDouble(params, semaine_str, semaine_str2, width, height);
				enregistrerImage(new URL(lienImage), chemin + semaine_str);
			}
		else
			// semestre printemps
			for(int i = no_semaine; i < 52; i += 2) {
				semaine_str = String.valueOf(i);
				semaine_str2 = String.valueOf(i + 1);
				choixEDTParmiReponsesADE(id_EDT);
				choixDesignParDefault();

				String params[] = creerLienVersImage();
				String lienImage = personnalisationLienSansWeekEndDouble(params, semaine_str, semaine_str2, width, height);
				enregistrerImage(new URL(lienImage), chemin + semaine_str);
			}
	}

	public void enregistrerICal(String id_annee, String recherche, String id_EDT, String chemin, String day, String month, String year,
		String end_day, String end_month, String end_year) throws ClientProtocolException, IOException {
		recupereTiquetADE();
		choisirAnneeADE(id_annee);
		rechercheADE(recherche);
		choixEDTParmiReponsesADE(id_EDT);
		choixDesignParDefault();

		InputStream is = requetePost("http://adeweb.uhp-nancy.fr/ade/custom/modules/plannings/ical.jsp", false, "startDay", day, "endDay", end_day,
			"endMonth", end_month, "endYear", end_year, "startMonth", month, "startYear", year, "calType", "ical");
		byte b[] = new byte[is.available()];
		is.read(b);
		FileWriter fw = new FileWriter(chemin + ".ical");
		fw.write(new String(b));
		fw.close();
	}

	private void simpleRequeteGET(String url) throws ClientProtocolException, IOException {
		HttpResponse response = httpclient.execute(new HttpGet(url));
		EntityUtils.consume(response.getEntity());
	}

	private String requeteGETRetour(String url) throws ClientProtocolException, IOException {
		HttpResponse response = httpclient.execute(new HttpGet(url));
		return Outils.istreamToString(response.getEntity().getContent());
	}

	private void enregistrerImage(URL lienImg, String chemin) throws MalformedURLException, IOException {
		Image img = ImageIO.read(lienImg);
		ImageIO.write((RenderedImage) img, "png", new File(chemin + ".png"));
	}

	@SuppressWarnings("unused")
	private String personnalisationLienSansWeekEnd(String[] params, String semaine, String width, String height) {
		String identifier = params[0];

		String finalLink = String.format("http://adeweb.uhp-nancy.fr/ade/imageEt?%s&%s&idPianoWeek=%s&%s&%s&width=%s&height=%s&%s&%s&%s&%s&%s",
			identifier, params[1], semaine, "idPianoDay=0%2C1%2C2%2C3%2C4", params[4], width, height, params[7], params[8], params[9], params[10],
			params[11]);
		return finalLink;
	}

	private String personnalisationLienSansWeekEndDouble(String[] params, String semaine1, String semaine2, String width, String height) {
		String identifier = params[0];

		String finalLink = String.format("http://adeweb.uhp-nancy.fr/ade/imageEt?%s&%s&idPianoWeek=%s%s&%s&%s&width=%s&height=%s&%s&%s&%s&%s&%s",
			identifier, params[1], semaine1+"%2C", semaine2, "idPianoDay=0%2C1%2C2%2C3%2C4", params[4], width, height, params[7], params[8], params[9],
			params[10], params[11]);
		return finalLink;
	}

	private String[] creerLienVersImage() throws ClientProtocolException, IOException {
		String page_entiere = requeteGETRetour("http://adeweb.uhp-nancy.fr/ade/custom/modules/plannings/imagemap.jsp?width=1600&height=800");
		String ss = Outils.chercherLigne(page_entiere, "<img border=0 src=");

		int deb = ss.indexOf("\"", 22);
		String REAL_LINK = (ss.substring(21, deb));

		return REAL_LINK.split("\\?")[1].split("&");
	}

	private void choixDesignParDefault() throws ClientProtocolException, IOException {
		simpleRequeteGET("http://adeweb.uhp-nancy.fr/ade/custom/modules/plannings/plannings.jsp");
	}

	private void choixEDTParmiReponsesADE(String id_EDT) throws ClientProtocolException, IOException {
		simpleRequeteGET("http://adeweb.uhp-nancy.fr/ade/standard/gui/tree.jsp?selectId=" + id_EDT + "&reset=true&forceLoad=false&scroll=0");
	}

	private void rechercheADE(String recherche) throws IOException {
		requetePost("http://adeweb.uhp-nancy.fr/ade/standard/gui/tree.jsp", true, "search", recherche);
	}

	private InputStream requetePost(String url, boolean consume, String... args) throws ClientProtocolException, IOException {
		HttpPost post = new HttpPost(url);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

		String tmp = null;
		for(String a : args)
			if(tmp == null)
				tmp = a;
			else {
				nameValuePairs.add(new BasicNameValuePair(tmp, a));
				tmp = null;
			}
		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		HttpResponse response = httpclient.execute(post);
		if(consume)
			EntityUtils.consume(response.getEntity());
		else
			return response.getEntity().getContent();
		return null;
	}

	private void choisirAnneeADE(String id_annee) throws IOException {
		// id_annee 2011-2012 : 1
		// id_annee 2010-2012 : 13
		requetePost("http://adeweb.uhp-nancy.fr/ade/standard/gui/interface.jsp?ticket=" + tiquetADE, true, "projectId", id_annee);
	}

	private void recupereTiquetADE() throws ClientProtocolException, IOException {
		HttpResponse response = httpclient.execute(new HttpGet("http://adeweb.uhp-nancy.fr/ade/standard/index.jsp"));

		String ss = (Outils.istreamToStringFind(response.getEntity().getContent(), "a id=\"serviceRedirect\" href=\""));
		int deb = ss.indexOf("\"", 45);
		ss = ss.substring(43, deb);

		tiquetADE = (ss.substring(65));
	}

	private void identificationUHP(String login, String password) throws ClientProtocolException, IOException {
		requetePost("https://cas.uhp-nancy.fr/cas/login;jsessionid=" + id
			+ "?service=http%3A%2F%2Fadeweb.uhp-nancy.fr%2Fade%2Fstandard%2Fgui%2Finterface.jsp", true, "username", login, "password", password,
			"lt", It, "_eventId", "submit");
	}

	private void recupererClesUHP() throws ClientProtocolException, IOException {

		String urlConnection = "https://cas.uhp-nancy.fr/cas/login?service=http%3A%2F%2Fadeweb.uhp-nancy.fr%2Fade%2Fstandard%2Fgui%2Finterface.jsp";

		HttpGet httpget = new HttpGet(urlConnection);
		HttpResponse response = httpclient.execute(httpget);

		String ss = (Outils.istreamToStringFind(response.getEntity().getContent(), "<input type=\"hidden\" name=\"lt\" value="));
		int deb = ss.indexOf("\"", 46);
		It = (ss.substring(44, deb));

		id = null;
		for(Cookie c : httpclient.getCookieStore().getCookies())
			if(c.getName().equalsIgnoreCase("jsessionid"))
				id = c.getValue();

	}

	private void creerClientHTTPS() {
		httpclient = Outils.wrapClient(new DefaultHttpClient());
	}

}
