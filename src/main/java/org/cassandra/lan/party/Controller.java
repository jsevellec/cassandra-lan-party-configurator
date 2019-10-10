package org.cassandra.lan.party;

import com.datastax.driver.core.Host;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.ipv6.IPv6Address;
import net.sf.uadetector.ReadableUserAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static inet.ipaddr.IPAddress.IPVersion.*;
import static net.sf.uadetector.OperatingSystemFamily.WINDOWS;

@org.springframework.stereotype.Controller
public class Controller {

    @Resource
    private CassandraService cassandraService;

    @Value("${ip.app.configurator}")
    private String appConfiguratorIp;

    public static final String DUMMY_USER_HOST = "10.1.1.0";

    @RequestMapping("/")
    public String home(Model model, HttpServletRequest request) {
        model.addAttribute("userIp", getUserIp(request));
        return "index";
    }

    @RequestMapping("/cassandra")
    public String goToCassandraPage(Model model, HttpServletRequest request, ReadableUserAgent userAgent) {
        boolean windowsClient = isWindowsClient(userAgent);
        model.addAttribute("isWindowsClient", windowsClient);
        model.addAttribute("listenAddress", "listen_address: " + getUserIp(request));
        model.addAttribute("rpcAddress", "rpc_address: " + getUserIp(request));
        if (windowsClient) {
            model.addAttribute("cqlshCmd", "bin\\cqlsh.bat " + getUserIp(request));
            model.addAttribute("startCassandraCmd", "bin\\cassandra.bat -f");
            model.addAttribute("nodetoolHelpCmd", "bin\\nodetool help");
            model.addAttribute("nodetoolStatusCmd", "bin\\nodetool status");
            model.addAttribute("nodetoolInfoCmd", "bin\\nodetool info");
        } else {
            model.addAttribute("cqlshCmd", "./bin/cqlsh " + getUserIp(request));
            model.addAttribute("startCassandraCmd", "./bin/cassandra -f");
            model.addAttribute("nodetoolHelpCmd", "./bin/nodetool help");
            model.addAttribute("nodetoolStatusCmd", "./bin/nodetool status");
            model.addAttribute("nodetoolInfoCmd", "./bin/nodetool info");
        }
        return "cassandra";
    }


    private boolean isWindowsClient(ReadableUserAgent userAgent) {
        return WINDOWS.equals(userAgent.getOperatingSystem().getFamily());
    }

    @RequestMapping("/archive")
    public String goToArchivePage(Model model, HttpServletRequest request) {
        return "archive";
    }

    @RequestMapping("/cluster")
    public String goToClusterPage(Model model, HttpServletRequest request) {
        Set<Host> allHosts = cassandraService.getHosts();
        List<Host> sortedHosts = allHosts
                .stream()
                .sorted((host1, host2) ->
                        (host1.getDatacenter() + host1.getBroadcastAddress().toString())
                                .compareTo(host2.getDatacenter() + host2.getBroadcastAddress().toString()))
                .collect(Collectors.toList());
        String node = "node";
        if (allHosts.size() > 1) {
            node = "nodes";
        }
        model.addAttribute("clusterSize", allHosts.size() + " " + node);
        model.addAttribute("hosts", sortedHosts);
        return "cluster";
    }


    private String getUserIp(HttpServletRequest request) {
        String userIp = request.getRemoteAddr();
        IPAddressString ipAddressString = new IPAddressString(userIp);
        if (ipAddressString.isIPv6()) {
            IPv6Address ipV6Address = (IPv6Address) ipAddressString.getAddress(IPV6);
            if(ipV6Address.isIPv4Compatible() || ipV6Address.isIPv4Mapped()) {
                userIp = ipV6Address.getEmbeddedIPv4Address().toString();
            }
        }

        return userIp.equals("127.0.0.1") || userIp.endsWith("1%0") ? DUMMY_USER_HOST : userIp;
    }
}
