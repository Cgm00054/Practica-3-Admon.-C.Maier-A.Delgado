package prueba;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

// bibliotecas a�adidas
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

// vamos a ver si funciona el GitHub 
// cambios en el archivo main

/**
 *
 * @author toni
 */
public class Main {
	

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception{
        ByteArrayInputStream bais=null;
        String user_min="";
       //read("cert.cer");
       
      // FileInputStream fis = new FileInputStream("cert.cer");
      
       
      // byte value[] = new byte[fis.available()];
       //  fis.read(value);
       // bais = new ByteArrayInputStream(value);

        //TODO: Obtener los datos del DNIe
        String NIF="";
        String nombre="";
        String ap1="";
        String ap2="";
        ObtenerDatos od = new ObtenerDatos();
        nombre=od.LeerNombre();
        ap1=od.LeerAp1();
        ap2=od.LeerAp2();
        NIF=od.LeerNIF();
        String password="123456";
        String SHA1 = "SHA-1";
        password=Mac.getStringMessageDigest(password, SHA1);
       System.out.println("Nombre: "+nombre);
       System.out.println("Apellido 1: "+ap1);
       System.out.println("Apellido 2: "+ap2);
       System.out.println("NIF: "+NIF);
       String nif = NIF.toLowerCase();
       
       String n1=""+nombre.charAt(0);
       String ap22=ap2.charAt(0)+"";
       String ap12="";
       for(int i=0;i<7;i++){
    	   ap12+=ap1.charAt(i);
       }
       String nom_usuario=n1+ap12+ap22;
       //System.out.println("USER:"+nom_usuario);
       user_min=nom_usuario.toLowerCase();
       System.out.println("username:" +user_min);
       System.out.println("password:"+password);
       
       //TODO: Autenticarse en el servidor
        
    // crear un metodo
       
       String userAgent = "Mozilla/5.0 (X11; Linux x86_64; rv:26.0) Gecko/20100101 Firefox/26.0";
       String address = "http://localhost/dnie/autentica.php";
       String forSending = user_min;
       String forSending2 = password;
       String forSending3 = nif;
       String charset = "UTF-8";

       // El metodo encode() de URLEncoder se encarga de encodear la cadena que enviaremos
       // al servidor, sustituyendo espacios y caracteres especiales
       String stringToSend = URLEncoder.encode(forSending, charset);
       String stringToSend2 = URLEncoder.encode(forSending2,charset);
       String stringToSend3 = URLEncoder.encode(forSending3,charset);

       // 1. Creamos objeto URL
       URL URL = new URL(address);
       // 2. Obtenemos el objeto URLConnection llamando a openConnection() en URL
       URLConnection connection = URL.openConnection();
       // Establecemos algunas propiedas de envi�, como es el User-Agent
       connection.addRequestProperty("User-Agent",userAgent );

       // 3. Esto es importantis�mo, es aqui donde establecemos la capacidad de envi�.
       connection.setDoOutput(true);

       // 4. Abrimos una conexi�n al recurso para poder escribir/enviar datos al formulario
       // Nota que no se llama expl�citamente a connect() porque llamados a getOutputStream()
       OutputStreamWriter out = new OutputStreamWriter(
               connection.getOutputStream());
       out.write("user=" + stringToSend+"&password="+stringToSend2+"&dni="+stringToSend3); // "nombre" es el campo del formulario web
       //out.write("password="+stringToSend2);
       out.close();

       // Aqu� leemos el resultado que nos devolvi� el servidor, en efecto, lo que
       // respondi� form.php y luego de enviar los datos
       BufferedReader in = new BufferedReader(
               new InputStreamReader(
                       connection.getInputStream()));
       String response;
       while((response = in.readLine()) != null){
    	   if(response.contains("Hey")==true){
    		   System.out.println(response);
    	   }else if(response.contains("No se encuentra")==true){
    		   System.out.println(response);
    	   }
          
       }
       in.close();
       
    }

}
