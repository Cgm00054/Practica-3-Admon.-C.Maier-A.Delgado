package prueba;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



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
       System.out.println("Nombre: "+nombre);
       System.out.println("Apellido 1: "+ap1);
       System.out.println("Apellido 2: "+ap2);
       System.out.println("NIF: "+NIF);
       String n1=""+nombre.charAt(0);
       String ap22=ap2.charAt(0)+"";
       String ap12="";
       for(int i=0;i<7;i++){
    	   ap12+=ap1.charAt(i);
       }
       String nom_usuario=n1+ap12+ap22;
       //System.out.println("USER:"+nom_usuario);
       String user_min=nom_usuario.toLowerCase();
       System.out.println("username:" +user_min);
       
       //TODO: Autenticarse en el servidor
        
    
    }

}
