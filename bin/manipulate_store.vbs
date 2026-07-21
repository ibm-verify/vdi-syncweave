workspacestring=WScript.Arguments.Item(0)
pos=InStrRev(workspacestring,";") -1
workspacestring=Left(workspacestring,pos)
pos=InStrRev(workspacestring,":") -2
workspacestring=Right(workspacestring,Len(workspacestring)-pos)
WScript.echo workspacestring
