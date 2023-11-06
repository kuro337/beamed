# k9

CLI Tool for Interacting with a k8 Cluster.

I find this tool incredibly handy and useful for interacting with k8 clusters.

<hr/>
<hr/>

Installing LinuxBrew if not present

```bash
# Installation using LinuxBrew
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
brew --version

# Install k9
brew install derailed/k9s/k9s
k9s help
```

<hr/>
<hr/>

Useful Commands

```bash
k9s

# Launch k9s for a namespace
k9s -o namespace

# Launch k9s for a pod
k9s -c pod

# Pod View
:pod

# Deployments View
:deploy

# Back
Escape

# Describe | View | Edit | View Logs
     d     |  v   | e    |    l
     
# Delete a Resource such as a Deployment, Pod, etc.
Ctrl + d -> TAB+ENTER Confirm
```

