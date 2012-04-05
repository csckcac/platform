/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.ec2client.utils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collection;

/**
 * ImportKey.java
 * <p/>
 * <p>This class imports a key and a certificate into a keystore
 * (<code>$home/keystore.ImportKey</code>). If the keystore is
 * already present, it is simply deleted. Both the key and the
 * certificate file must be in <code>DER</code>-format. The key must be
 * encoded with <code>PKCS#8</code>-format. The certificate must be
 * encoded in <code>X.509</code>-format.</p>
 * <p/>
 * <p>Key format:</p>
 * <p><code>openssl pkcs8 -topk8 -nocrypt -in YOUR.KEY -out YOUR.KEY.der
 * -outform der</code></p>
 * <p>Format of the certificate:</p>
 * <p><code>openssl x509 -in YOUR.CERT -out YOUR.CERT.der -outform
 * der</code></p>
 * <p>Import key and certificate:</p>
 * <p><code>java comu.ImportKey YOUR.KEY.der YOUR.CERT.der</code></p><br />
 * <p/>
 * <p><em>Caution:</em> the old <code>keystore.ImportKey</code>-file is
 * deleted and replaced with a keystore only containing <code>YOUR.KEY</code>
 * and <code>YOUR.CERT</code>. The keystore and the key has no password;
 * they can be set by the <code>keytool -keypasswd</code>-command for setting
 * the key password, and the <code>keytool -storepasswd</code>-command to set
 * the keystore password.
 * <p>The key and the certificate is stored under the alias
 * <code>importkey</code>; to change this, use <code>keytool -keyclone</code>.
 * <p/>
 * Created: Fri Apr 13 18:15:07 2001
 * Updated: Fri Apr 19 11:03:00 2002
 *
 * @author Joachim Karrer, Jens Carlberg
 * @version 1.1
 */
public class KeyImporter {

    /**
     * <p>Creates an InputStream from a file, and fills it with the complete
     * file. Thus, available() on the returned InputStream will return the
     * full number of bytes the file contains</p>
     *
     * @param fname The filename
     * @return The filled InputStream
     * @throws IOException, if the Streams couldn't be created.
     */
    private static InputStream fullStream(String fname) throws IOException {
        FileInputStream fis = new FileInputStream(fname);
        DataInputStream dis = new DataInputStream(fis);
        byte[] bytes = new byte[dis.available()];
        dis.readFully(bytes);
        return new ByteArrayInputStream(bytes);
    }

    public static void doImport(String keystoreName,
                                String privateKeyFile,
                                String certFile,
                                String defaultAlias,
                                String keypass) {

        try {
            // initializing and clearing keystore
            KeyStore ks = KeyStore.getInstance("JKS", "SUN");
            ks.load(null, keypass.toCharArray());
            ks.store(new FileOutputStream(keystoreName),
                     keypass.toCharArray());
            ks.load(new FileInputStream(keystoreName),
                    keypass.toCharArray());

            // loading Key
            InputStream fl = fullStream(privateKeyFile);
            byte[] key = new byte[fl.available()];
            KeyFactory kf = KeyFactory.getInstance("RSA");
            fl.read(key, 0, fl.available());
            fl.close();
            PKCS8EncodedKeySpec keysp = new PKCS8EncodedKeySpec(key);
            PrivateKey ff = kf.generatePrivate(keysp);

            // loading CertificateChain
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream certstream = fullStream(certFile);

            Collection c = cf.generateCertificates(certstream);
            Certificate[] certs = new Certificate[c.toArray().length];

            if (c.size() == 1) {
                certstream = fullStream(certFile);
                Certificate cert = cf.generateCertificate(certstream);
                certs[0] = cert;
            } else {
                certs = (Certificate[]) c.toArray();
            }

            // storing keystore
            ks.setKeyEntry(defaultAlias, ff,
                           keypass.toCharArray(),
                           certs);
            ks.store(new FileOutputStream(keystoreName), keypass.toCharArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}