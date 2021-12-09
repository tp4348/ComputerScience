#!/bin/sh

# Make sure only root can run our script
if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi

# Exit immediately if a command exits with a non-zero status.
set -e

# Disable IPv6
sudo tee -a /etc/sysctl.conf <<EOF
net.ipv6.conf.all.disable_ipv6 = 1
net.ipv6.conf.default.disable_ipv6 = 1
net.ipv6.conf.lo.disable_ipv6 = 1
EOF

sudo sysctl -p

# Disable all chains, therefore only traffic that we allow is passed through
sudo iptables --policy INPUT DROP
sudo iptables --policy OUTPUT DROP
sudo iptables --policy FORWARD DROP

# Remove any existing rules from all chains
sudo iptables --flush
sudo iptables -t nat --flush
sudo iptables -t mangle --flush

# Delete any user-defined chains
sudo iptables -X
sudo iptables -t nat -X
sudo iptables -t mangle -X

# Reset all counters to zero
sudo iptables -Z

### Allow all trafic on localhost
sudo iptables -A INPUT -i lo -j ACCEPT
sudo iptables -A OUTPUT -o lo -j ACCEPT

# ESTABLISH-RELATED trick: Allow all incoming packets that belong to ESTABLISHED or RELATED connections.
# From here onwards, we can add incoming firewall exceptions using only the NEW state
sudo iptables -A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT
sudo iptables -A OUTPUT -m state --state ESTABLISHED,RELATED -j ACCEPT

# Allow incomming connections to local SSH server
sudo iptables -A INPUT -p tcp --dport 22 -m state --state NEW -j ACCEPT

# Allow outgoing ping requests
sudo iptables -A OUTPUT -p icmp --icmp-type echo-request -m state --state NEW -j ACCEPT
sudo iptables -A INPUT -p icmp --icmp-type echo-reply -m state --state NEW -j ACCEPT
# Allow incoming ping requests
sudo iptables -A INPUT -p icmp --icmp-type echo-request -m state --state NEW -j ACCEPT
sudo iptables -A OUTPUT -p icmp --icmp-type echo-reply -m state --state NEW -j ACCEPT

# TODO: OTA update
# TODO: All things need to be run on localhost, for multiple robots all ports needs to open in ROS1

exit 0
