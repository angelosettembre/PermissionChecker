package com.isislab.settembre.privacychecker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XMLFile {
    private byte[] PERMISSION_TAG = new byte[]{(byte) 20};
    private byte[] XML_START_TAG;
    private byte[] data;
    private int filesize;
    public String log = "";
    public int permissions = 0;
    private List<Permesso> permls = new ArrayList();
    private RWFile rwfile = new RWFile();
    public String[] string;

    public XMLFile(String filename) {
        byte[] bArr = new byte[4];
        bArr[1] = (byte) 16;
        bArr[2] = (byte) 1;
        bArr[3] = (byte) 2;
        this.XML_START_TAG = bArr;
        try {
            int next;
            System.out.println("CI ASDSDDDDDD 22222");
            this.data = this.rwfile.ReadFile(filename);                     //Lettura del file; e ritorna un array di byte contente la lunghezza del "filename"
            this.filesize = this.data.length;                               //Lunghezza dell'array
            this.log = Integer.toString(this.filesize);
            int strings = Conv(this.data, 16, 4);                           //Shift a sinistra
            this.string = new String[strings];                              //Array di stringhe
            for (int i = 0; i < strings; i++) {
                this.string[i] = "";
            }
            int ofs = Conv(this.data, 28, 4) + 8;
            int stringend = Conv(this.data, 12, 4);
            int stringid = 0;
            while (ofs < stringend) {
                next = Conv(this.data, ofs, 2) * 2;
                this.string[stringid] = LoadString(this.data, ofs);
                ofs += (next + 2) + 2;
                stringid++;
                if (stringid >= strings) {
                    break;
                }
            }
            System.out.println("USCITO DAL WHILEEEE");
            ofs = 8;
            while (ofs < this.filesize) {
                next = Conv(this.data, ofs + 4, 4);

                if (Compare(this.data, ofs, this.XML_START_TAG, 4).booleanValue()) {
                    if (this.string[Conv(this.data, ofs + 20, 4)].equals("uses-permission")) {
                        int id = Conv(this.data, ofs + 44, 4);
                        if (id < this.string.length) {
                            this.permls.add(new Permesso(this.data[ofs + 44], this.string[id], ofs, Conv(this.data, (ofs + next) + 4, 4) + next));            //Aggiungi alla lista dei permessi
                        }
                    }
                }
                ofs += next;
                if (next == 0) {
                    break;
                }
            }
            this.log = Integer.toString(strings);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String LoadString(byte[] data, int ofs) {
        String ret = "";
        int len = Conv(data, ofs, 2) * 2;                               //Shift a sinistra
        for (int i = 0; i < len; i += 2) {
            try{
                ret = new StringBuilder(String.valueOf(ret)).append((char) data[(ofs + i) + 2]).toString();
            } catch (ArrayIndexOutOfBoundsException e){
                ret="";
            }
        }
        return ret;
    }

    private Boolean Compare(byte[] data, int ofs, byte[] data2, int len) {
        for (int i = 0; i < len; i++) {
            if (data[ofs + i] != data2[(len - 1) - i]) {
                return Boolean.valueOf(false);
            }
        }
        return Boolean.valueOf(true);
    }

    private int Conv(byte[] data, int ofs, int len) {                       //data = lunghezza Manifest; 16 ofs = offset ; 4 len = lunghezza
        int shift = 0;
        int ret = 0;
        for (int i = 0; i < len; i++) {
            ret += (data[ofs + i] & 255) << shift;                                  //Spostamento a sinistra di "(data[ofs + i] & 255))" di "shift" bit
            shift += 8;                                                             //Spostamento di 8 bit a sinistra -> data[ofs + i] & 255)00000000
        }
        return ret;
    }

    public List<Permesso> GetPermisionList() {
        return this.permls;
    }

    public Boolean WriteFile(String name) {
        try {
            return this.rwfile.WriteFile(name, this.data);                               //data = this.pathTmp + "AndroidManifest.xml"
        } catch (Exception e) {
            return Boolean.valueOf(false);
        }
    }

    public void RemovePermission(int id) {                          //Metodo per rimuovere il permesso
        Permesso p = (Permesso) this.permls.get(id);            //Id del permesso dalla lista permessi
        byte[] data2 = new byte[(this.data.length - p.len)];
        int i2 = 0;
        int i = 0;
        while (i < this.data.length) {
            if (i < p.ofs || i >= p.ofs + p.len) {
                int i22 = i2 + 1;
                data2[i2] = this.data[i];
                i2 = i22;
            }
            i++;
        }
        data2[4] = (byte) (data2.length & 255);
        data2[5] = (byte) ((data2.length >> 8) & 255);
        data2[6] = (byte) ((data2.length >> 16) & 255);
        data2[7] = (byte) ((data2.length >> 24) & 255);
        this.data = data2;
        p.removed = true;
        for (i = 0; i < this.permls.size(); i++) {
            Permesso p2 = (Permesso) this.permls.get(i);
            if (!p2.removed && p2.ofs > p.ofs) {
                p2.ofs -= p.len;
            }
        }
    }

    @Override
    public String toString() {
        return "XMLFile{" +
                "PERMISSION_TAG=" + Arrays.toString(PERMISSION_TAG) +
                ", XML_START_TAG=" + Arrays.toString(XML_START_TAG) +
                ", data=" + Arrays.toString(data) +
                ", filesize=" + filesize +
                ", log='" + log + '\'' +
                ", permissions=" + permissions +
                ", permls=" + permls +
                ", rwfile=" + rwfile +
                ", string=" + Arrays.toString(string) +
                '}';
    }
}
