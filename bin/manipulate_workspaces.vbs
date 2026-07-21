workspacestring=WScript.Arguments.Item(0)
workspacestring=Replace(workspacestring,"\\","\")
workspacestring=Replace(workspacestring,"\:",":")
workspaces=Split(workspacestring, "\n")
workspaces=Join(workspaces,"?")
WScript.echo workspaces
