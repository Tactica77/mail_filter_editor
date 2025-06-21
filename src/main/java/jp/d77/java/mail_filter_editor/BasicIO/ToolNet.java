package jp.d77.java.mail_filter_editor.BasicIO;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ToolNet {
    private static HashMap<String, WhoisResult> whois_result = new HashMap<String, WhoisResult>();

    public static Optional<WhoisResult> getWhois( String ipAddress ){
        // キャッシュを返す
        if ( ToolNet.whois_result.containsKey(ipAddress) ) return Optional.ofNullable( ToolNet.whois_result.get(ipAddress) );

        WhoisResult wr = new WhoisResult();
        ToolNet.whois_result.put(ipAddress, wr);
        wr.setIp(ipAddress);

        Debugger.TracePrint();
        String whoisServer = "whois.iana.org";

        try (Socket socket = new Socket(whoisServer, 43);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // まず、IANAでIPアドレスの割り当て先（RIR）を取得
            out.println(ipAddress);

            String line;
            while ((line = in.readLine()) != null) {
                wr.addIanaResult( line );
                //System.out.println(line);
                if (line.toLowerCase().startsWith("refer:")) {
                    wr.setRIR( line.split(":")[1].trim() );
                }
            }
            if (wr.getRIR().isEmpty() ) return Optional.empty();
            
            //System.out.println("\n--- 接続先 RIR: " + whoisRedirectServer + " ---\n");

            // 取得したRIRに再度接続して詳細なWHOISを取得
            try (Socket redirectSocket = new Socket( wr.getRIR().get(), 43);
                    PrintWriter redirectOut = new PrintWriter(redirectSocket.getOutputStream(), true);
                    BufferedReader redirectIn = new BufferedReader(new InputStreamReader(redirectSocket.getInputStream()))) {

                redirectOut.println(ipAddress);

                while ((line = redirectIn.readLine()) != null) {
                    wr.addWhoisResult( line );
                    //System.out.println(line);
                }
            }

            //System.out.println( wr.getWhoisResult() );
            //wr.dump();
            Debugger.LogPrint( "IP=" + ipAddress + " RIR=" + wr.getRIR().orElse("-") );
            return Optional.ofNullable( wr );
        } catch (Exception e) {
            wr.setError( e );
            e.printStackTrace();
            return Optional.ofNullable( wr );
        }

    }

    /**
     * 255.255.255.0→24
     * @param mask
     * @return
     * @throws Exception
     */
    public static int maskToCidr(String mask) throws Exception {
        byte[] bytes = InetAddress.getByName(mask).getAddress();
        int cidr = 0;
        for (byte b : bytes) {
            int bits = b & 0xFF;
            while ((bits & 0x80) != 0) {
                cidr++;
                bits <<= 1;
            }
        }
        return cidr;
    }

    /**
     * ipの数値表現をnnn.nnn.nnn.nnnへ変換
     * @param ip
     * @return
     */
    private static String longToIP(long ip) {
        return String.format("%d.%d.%d.%d",
            (ip >> 24) & 0xFF,
            (ip >> 16) & 0xFF,
            (ip >> 8) & 0xFF,
            ip & 0xFF);
    }

    /**
     * ipの10進数表現(nnn.nnn.nnn.nnn)を数値表現をへ変換
     * @param ip
     * @return
     */
    private static long ipToLong(String ip) {
        String[] parts = ip.split("\\.");
        long res = 0;
        for (String part : parts) {
            res = res * 256 + Integer.parseInt(part);
        }
        return res;
    }

    /**
     * /24→255.255.255.0
     * @param cidr
     * @return
     */
    public static String Cidr2MaskString( int cidr ){
        int mask = 0xffffffff << (32 - cidr); // 上位cidrビットを1にする
        int octet1 = (mask >>> 24) & 0xff;
        int octet2 = (mask >>> 16) & 0xff;
        int octet3 = (mask >>> 8) & 0xff;
        int octet4 = mask & 0xff;
        return String.format("%d.%d.%d.%d", octet1, octet2, octet3, octet4);        
    }

    /**
     * CIDR値から数値表現へ変換
     * @param cidr
     * @return
     */
    private static long Cidr2MaskLong(int cidr) {
        return 0xFFFFFFFFL << (32 - cidr);
    }

    /**
     * IPとCIDRから、IPレンジを返す
     * @param ip
     * @param cidr
     * @return [0]=Start IP,[1]=End IP
     */
    public static String[] calculateRange(String ip, int cidr) {
        try{
            InetAddress ipAddress = InetAddress.getByName(ip);
            byte[] ipBytes = ipAddress.getAddress();

            int mask = 0xffffffff << (32 - cidr);
            byte[] maskBytes = new byte[] {
                (byte) (mask >>> 24),
                (byte) (mask >>> 16),
                (byte) (mask >>> 8),
                (byte) mask
            };

            byte[] networkBytes = new byte[4];
            byte[] broadcastBytes = new byte[4];

            for (int i = 0; i < 4; i++) {
                networkBytes[i] = (byte) (ipBytes[i] & maskBytes[i]);
                broadcastBytes[i] = (byte) (ipBytes[i] | ~maskBytes[i]);
            }

            InetAddress start = InetAddress.getByAddress(networkBytes);
            InetAddress end = InetAddress.getByAddress(broadcastBytes);

            String res[] = new String[2];
            res[0] = start.getHostAddress();
            res[1] = end.getHostAddress();
            //System.out.printf("CIDR: %s/%d%n", ip, cidr);
            //System.out.println("Start IP: " + start.getHostAddress());
            //System.out.println("End IP:   " + end.getHostAddress());
            return res;
        }catch( UnknownHostException e ){
            e.printStackTrace();
            String res[] = new String[2];
            res[0] = "";
            res[1] = "";
            return res;
        }
    }
    
    /**
     * IPレンジからCIDRへ変換する。複数の結果を返すこともある
     * @param startIP
     * @param endIP
     * @return
     */
    public static List<String> rangeToCIDRs(String startIP, String endIP) {
        long start = ToolNet.ipToLong(startIP);
        long end = ToolNet.ipToLong(endIP);
        List<String> cidrs = new ArrayList<>();
        while (start <= end) {
            byte maxSize = 32;
            while (maxSize > 0) {
                long mask = ToolNet.Cidr2MaskLong(maxSize - 1);
                long maskedBase = start & mask;
                if (maskedBase != start) break;
                maxSize--;
            }

            double x = Math.log(end - start + 1) / Math.log(2);
            byte maxDiff = (byte)(32 - Math.floor(x));
            if (maxSize < maxDiff) maxSize = maxDiff;

            cidrs.add(longToIP(start) + "/" + maxSize);
            start += Math.pow(2, (32 - maxSize));
        }
        return cidrs;
    }

    /**
     * 様々な形のIPレンジから、CIDRへ変換する
     * @param input
     * @return
     * @throws Exception
     */
    public static List<String> convertToCIDR(String input) {
        input = input.trim();

        if (input.contains("-")) {
            // 例: 156.244.56.0-156.244.57.255
            String[] parts = input.replace(" ", "").split("-");
            return ToolNet.rangeToCIDRs(parts[0], parts[1]);

        } else if (input.matches(".+/\\d+")) {
            // 例: 156.244.57.114/23（そのままCIDR）
            return List.of(input);

        } else if (input.matches(".+/\\d+\\.\\d+\\.\\d+\\.\\d+")) {
            // 例: 156.244.56.0/255.255.254.0 → CIDRに変換
            String[] parts = input.split("/");
            int cidr;
            try {
                cidr = ToolNet.maskToCidr(parts[1]);
                return List.of(parts[0] + "/" + cidr);
            } catch (Exception e) {
                return List.of(new String[0]);
            }

        } else {
            return List.of(new String[0]);
        }
    }

    /**
     * 片方のCIDRがもう片方の範囲かを判定し、範囲の広い方を返す。範囲に含まれない場合はempty
     * @param cidrA
     * @param cidrB
     * @return 広い方の範囲を返す。完全一致の場合はcidrAを返します
     */
    public static Optional<String> isWithinCIDR(String cidrA, String cidrB) {
        String[] aParts = cidrA.split("/");
        String[] bParts = cidrB.split("/");

        try {
            int prefixA,prefixB;
            if ( aParts.length <= 1 ){
                prefixA = 32;
            }else{
                prefixA = Integer.parseInt(aParts[1]);
            }
            if ( bParts.length <= 1 ){
                prefixB = 32;
            }else{
                prefixB = Integer.parseInt(bParts[1]);
            }

            long ipA = ipToLong(aParts[0]);
            long ipB = ipToLong(bParts[0]);

            long networkA = ipA & maskBits(prefixA);
            long networkB = ipB & maskBits(prefixB);

            // BがAに含まれる
            if (prefixB >= prefixA && (ipB & maskBits(prefixA)) == networkA) {
                return Optional.of(cidrA);
            }
            // AがBに含まれる
            if (prefixA >= prefixB && (ipA & maskBits(prefixB)) == networkB) {
                return Optional.of(cidrB);
            }
            return Optional.empty();
        } catch (Exception e) {
            Debugger.LogPrint( "cmp error: " + cidrA + " " + cidrB + " " + e.getMessage() );
            return Optional.empty();
        }
    }

    private static long maskBits(int prefix) {
        return ~((1L << (32 - prefix)) - 1) & 0xFFFFFFFFL;
    }
}
