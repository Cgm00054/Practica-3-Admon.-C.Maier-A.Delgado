package prueba;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

/**
 * La clase ObtenerDatos implementa cuatro métodos públicos que permiten obtener
 * determinados datos de los certificados de tarjetas DNIe, Izenpe y Ona.
 *
 * @author tbc
 */
// Cambios en el archivo ObtenerDatos.java


public class ObtenerDatos {

    private static final byte[] dnie_v_1_0_Atr = {
        (byte) 0x3B, (byte) 0x7F, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x6A, (byte) 0x44,
        (byte) 0x4E, (byte) 0x49, (byte) 0x65, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x90, (byte) 0x00};
    private static final byte[] dnie_v_1_0_Mask = {
        (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF};

    public ObtenerDatos() {
    }

    // m�todo para recoger el nombre del DNIe:
    public String LeerNombre() {
        String nom = null;
        try {
            Card c = ConexionTarjeta();
            if (c == null) {
                throw new Exception("No se ha encontrado ninguna tarjeta");
            }
            byte[] atr = c.getATR().getBytes();
            CardChannel ch = c.getBasicChannel();

            if (esDNIe(atr)) {
                nom=leerDeCertificadoNom(ch);
            }
            c.disconnect(false);

        } catch (Exception ex) {
            Logger.getLogger(ObtenerDatos.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        return nom;
    }


    public String leerDeCertificadoNom(CardChannel ch) throws CardException {
        int offset = 0;
        String nom = null;

        byte[] command = new byte[]{(byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x0b, (byte) 0x4D, (byte) 0x61, (byte) 0x73, (byte) 0x74, (byte) 0x65, (byte) 0x72, (byte) 0x2E, (byte) 0x46, (byte) 0x69, (byte) 0x6C, (byte) 0x65};
        ResponseAPDU r = ch.transmit(new CommandAPDU(command));
        if ((byte) r.getSW() != (byte) 0x9000) {
            System.out.println("SW incorrecto");
            return null;
        }

        //Seleccionamos el directorio PKCS#15 5015
        command = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x50, (byte) 0x15};
        r = ch.transmit(new CommandAPDU(command));

        if ((byte) r.getSW() != (byte) 0x9000) {
            System.out.println("SW incorrecto");
            return null;
        }

        //Seleccionamos el Certificate Directory File (CDF) del DNIe 6004
        command = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x60, (byte) 0x04};
        r = ch.transmit(new CommandAPDU(command));

        if ((byte) r.getSW() != (byte) 0x9000) {
            System.out.println("SW incorrecto");
           return null;
        }

        //Leemos FF bytes del archivo
        command = new byte[]{(byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0x00, (byte) 0xFF};
        r = ch.transmit(new CommandAPDU(command));

        if ((byte) r.getSW() == (byte) 0x9000) {
            byte[] datos = r.getData();

            if (datos[4] == 0x30) {
                offset = 4;
                offset += datos[offset + 1] + 2; //Obviamos la seccion del Label
            }

            if (datos[offset] == 0x30) {
                offset += datos[offset + 1] + 2; //Obviamos la seccion de la informacion sobre la fecha de expedición etc
            }

            if ((byte) datos[offset] == (byte) 0xA1) {
                //El certificado empieza aquí
                byte[] r3 = new byte[9];

                //Nos posicionamos en el byte donde empieza el NIF y leemos sus 9 bytes
                // para el nombre
                for (int z = 0; z < 7; z++) {
                    r3[z] = datos[147 + z];
                }
                nom = new String(r3);
            }
        }
        
        return nom;
    }

    //m�todo para recoger apellido 1:
    public String LeerAp1() {
        String ap1 = null;
        try {
            Card c = ConexionTarjeta();
            if (c == null) {
                throw new Exception("No se ha encontrado ninguna tarjeta");
            }
            byte[] atr = c.getATR().getBytes();
            CardChannel ch = c.getBasicChannel();

            if (esDNIe(atr)) {
                ap1=leerDeCertificadoAp1(ch);
            }
            c.disconnect(false);

        } catch (Exception ex) {
            Logger.getLogger(ObtenerDatos.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        return ap1;
    }


    public String leerDeCertificadoAp1(CardChannel ch) throws CardException {
        int offset = 0;
        String ap1 = null;

        byte[] command = new byte[]{(byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x0b, (byte) 0x4D, (byte) 0x61, (byte) 0x73, (byte) 0x74, (byte) 0x65, (byte) 0x72, (byte) 0x2E, (byte) 0x46, (byte) 0x69, (byte) 0x6C, (byte) 0x65};
        ResponseAPDU r = ch.transmit(new CommandAPDU(command));
        if ((byte) r.getSW() != (byte) 0x9000) {
            System.out.println("SW incorrecto");
            return null;
        }

        //Seleccionamos el directorio PKCS#15 5015
        command = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x50, (byte) 0x15};
        r = ch.transmit(new CommandAPDU(command));

        if ((byte) r.getSW() != (byte) 0x9000) {
            System.out.println("SW incorrecto");
            return null;
        }

        //Seleccionamos el Certificate Directory File (CDF) del DNIe 6004
        command = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x60, (byte) 0x04};
        r = ch.transmit(new CommandAPDU(command));

        if ((byte) r.getSW() != (byte) 0x9000) {
            System.out.println("SW incorrecto");
           return null;
        }

        //Leemos FF bytes del archivo
        command = new byte[]{(byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0x00, (byte) 0xFF};
        r = ch.transmit(new CommandAPDU(command));

        if ((byte) r.getSW() == (byte) 0x9000) {
            byte[] datos = r.getData();

            if (datos[4] == 0x30) {
                offset = 4;
                offset += datos[offset + 1] + 2; //Obviamos la seccion del Label
            }

            if (datos[offset] == 0x30) {
                offset += datos[offset + 1] + 2; //Obviamos la seccion de la informacion sobre la fecha de expedición etc
            }

            if ((byte) datos[offset] == (byte) 0xA1) {
                //El certificado empieza aquí
                byte[] r3 = new byte[9];

                
                
                //Nos posicionamos en el byte donde empieza el apellido 1 y leemos sus bytes
 
                for (int z = 0; z < 7; z++) {
                    r3[z] = datos[165 + z];
                }
                ap1 = new String(r3);
            }
        }
        
        return ap1;
    }
    
    // m�todo recoger apellido 2:
    
    public String LeerAp2() {
        String ap2 = null;
        try {
            Card c = ConexionTarjeta();
            if (c == null) {
                throw new Exception("No se ha encontrado ninguna tarjeta");
            }
            byte[] atr = c.getATR().getBytes();
            CardChannel ch = c.getBasicChannel();

            if (esDNIe(atr)) {
                ap2=leerDeCertificadoAp2(ch);
            }
            c.disconnect(false);

        } catch (Exception ex) {
            Logger.getLogger(ObtenerDatos.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        return ap2;
    }


    public String leerDeCertificadoAp2(CardChannel ch) throws CardException {
        int offset = 0;
        String ap2 = null;

        byte[] command = new byte[]{(byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x0b, (byte) 0x4D, (byte) 0x61, (byte) 0x73, (byte) 0x74, (byte) 0x65, (byte) 0x72, (byte) 0x2E, (byte) 0x46, (byte) 0x69, (byte) 0x6C, (byte) 0x65};
        ResponseAPDU r = ch.transmit(new CommandAPDU(command));
        if ((byte) r.getSW() != (byte) 0x9000) {
            System.out.println("SW incorrecto");
            return null;
        }

        //Seleccionamos el directorio PKCS#15 5015
        command = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x50, (byte) 0x15};
        r = ch.transmit(new CommandAPDU(command));

        if ((byte) r.getSW() != (byte) 0x9000) {
            System.out.println("SW incorrecto");
            return null;
        }

        //Seleccionamos el Certificate Directory File (CDF) del DNIe 6004
        command = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x60, (byte) 0x04};
        r = ch.transmit(new CommandAPDU(command));

        if ((byte) r.getSW() != (byte) 0x9000) {
            System.out.println("SW incorrecto");
           return null;
        }

        //Leemos FF bytes del archivo
        command = new byte[]{(byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0x00, (byte) 0xFF};
        r = ch.transmit(new CommandAPDU(command));

        if ((byte) r.getSW() == (byte) 0x9000) {
            byte[] datos = r.getData();

            if (datos[4] == 0x30) {
                offset = 4;
                offset += datos[offset + 1] + 2; //Obviamos la seccion del Label
            }

            if (datos[offset] == 0x30) {
                offset += datos[offset + 1] + 2; //Obviamos la seccion de la informacion sobre la fecha de expedición etc
            }

            if ((byte) datos[offset] == (byte) 0xA1) {
                //El certificado empieza aquí
                byte[] r3 = new byte[9];

                
                //Nos posicionamos en el byte donde empieza el NIF y leemos sus 9 bytes
 
                for (int z = 0; z < 5; z++) {
                    r3[z] = datos[173 + z];
                }
                ap2 = new String(r3);
                
            }
        }
        
        return ap2;
    }
   
    // m�todo recoger NIF:
    
    public String LeerNIF() {
        String nif = null;
        try {
            Card c = ConexionTarjeta();
            if (c == null) {
                throw new Exception("No se ha encontrado ninguna tarjeta");
            }
            byte[] atr = c.getATR().getBytes();
            CardChannel ch = c.getBasicChannel();

            if (esDNIe(atr)) {
                nif=leerDeCertificadoNIF(ch);
            }
            c.disconnect(false);

        } catch (Exception ex) {
            Logger.getLogger(ObtenerDatos.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        return nif;
    }


    public String leerDeCertificadoNIF(CardChannel ch) throws CardException {
        int offset = 0;
        String nif = null;

        byte[] command = new byte[]{(byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x0b, (byte) 0x4D, (byte) 0x61, (byte) 0x73, (byte) 0x74, (byte) 0x65, (byte) 0x72, (byte) 0x2E, (byte) 0x46, (byte) 0x69, (byte) 0x6C, (byte) 0x65};
        ResponseAPDU r = ch.transmit(new CommandAPDU(command));
        if ((byte) r.getSW() != (byte) 0x9000) {
            System.out.println("SW incorrecto");
            return null;
        }

        //Seleccionamos el directorio PKCS#15 5015
        command = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x50, (byte) 0x15};
        r = ch.transmit(new CommandAPDU(command));

        if ((byte) r.getSW() != (byte) 0x9000) {
            System.out.println("SW incorrecto");
            return null;
        }

        //Seleccionamos el Certificate Directory File (CDF) del DNIe 6004
        command = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x60, (byte) 0x04};
        r = ch.transmit(new CommandAPDU(command));

        if ((byte) r.getSW() != (byte) 0x9000) {
            System.out.println("SW incorrecto");
           return null;
        }

        //Leemos FF bytes del archivo
        command = new byte[]{(byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0x00, (byte) 0xFF};
        r = ch.transmit(new CommandAPDU(command));

        if ((byte) r.getSW() == (byte) 0x9000) {
            byte[] datos = r.getData();

            if (datos[4] == 0x30) {
                offset = 4;
                offset += datos[offset + 1] + 2; //Obviamos la seccion del Label
            }

            if (datos[offset] == 0x30) {
                offset += datos[offset + 1] + 2; //Obviamos la seccion de la informacion sobre la fecha de expedición etc
            }

            if ((byte) datos[offset] == (byte) 0xA1) {
                //El certificado empieza aquí
                byte[] r3 = new byte[9];

                
                //Nos posicionamos en el byte donde empieza el NIF y leemos sus 9 bytes
 
                for (int z = 0; z < 9; z++) {
                    r3[z] = datos[109 + z];
                }
                nif = new String(r3);
                
            }
        }
        
        return nif;
    }

    /**
     * Este método establece la conexión con la tarjeta. La función busca el
     * Terminal que contenga una tarjeta, independientemente del tipo de tarjeta
     * que sea.
     *
     * @return objeto Card con conexión establecida
     * @throws Exception
     */
    private Card ConexionTarjeta() throws Exception {

        Card card = null;
        TerminalFactory factory = TerminalFactory.getDefault();
        List<CardTerminal> terminals = factory.terminals().list();
        //System.out.println("Terminals: " + terminals);

        for (int i = 0; i < terminals.size(); i++) {

            // get terminal
            CardTerminal terminal = terminals.get(i);

            try {
                if (terminal.isCardPresent()) {
                    card = terminal.connect("*"); //T=0, T=1 or T=CL(not needed)
                }
            } catch (CardException e) {

                System.out.println("Exception catched: " + e.getMessage());
                card = null;
            }
        }
        return card;
    }

    /**
     * Este método nos permite saber el tipo de tarjeta que estamos leyendo del
     * Terminal, a partir del ATR de ésta.
     *
     * @param atrCard ATR de la tarjeta que estamos leyendo
     * @return tipo de la tarjeta. 1 si es DNIe, 2 si es Starcos y 0 para los
     * demás tipos
     */
    private boolean esDNIe(byte[] atrCard) {
        int j = 0;
        boolean found = false;

        //Es una tarjeta DNIe?
        if (atrCard.length == dnie_v_1_0_Atr.length) {
            found = true;
            while (j < dnie_v_1_0_Atr.length && found) {
                if ((atrCard[j] & dnie_v_1_0_Mask[j]) != (dnie_v_1_0_Atr[j] & dnie_v_1_0_Mask[j])) {
                    found = false; //No es una tarjeta DNIe
                }
                j++;
            }
        }

        if (found == true) {
            return true;
        } else {
            return false;
        }
    }
    
    // acceder al dni con http:
    
}
