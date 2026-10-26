import java.nio.file.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;
import java.util.jar.*;
import java.io.*;

/** Verify every payload entry and compare its signer with the exported upload certificate. */
class VerifyBundleSignature {
    public static void main(String[] args) throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        byte[] expected;
        try (InputStream in = Files.newInputStream(Path.of(args[1]))) {
            expected = factory.generateCertificate(in).getEncoded();
        }
        int verified = 0;
        try (JarFile jar = new JarFile(args[0], true)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;
                try (InputStream in = jar.getInputStream(entry)) { in.transferTo(OutputStream.nullOutputStream()); }
                if (entry.getName().startsWith("META-INF/")) continue;
                CodeSigner[] signers = entry.getCodeSigners();
                if (signers == null || signers.length != 1) throw new SecurityException("Unsigned or unexpected signer: " + entry.getName());
                byte[] actual = signers[0].getSignerCertPath().getCertificates().get(0).getEncoded();
                if (!Arrays.equals(expected, actual)) throw new SecurityException("Unexpected upload certificate");
                verified++;
            }
        }
        System.out.println("PASS: " + verified + " bundle payload entries verified against the exported upload certificate");
    }
}
