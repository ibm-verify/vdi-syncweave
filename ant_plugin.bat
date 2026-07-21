rem run this bat to do ant build and build plugin package(s)
@call d:\jakarta-ant-1.5\bin\ant -buildfile build_mqestore_plugin.xml %1
@call d:\jakarta-ant-1.5\bin\ant -buildfile build_pwstore_plugin.xml %1
@call d:\jakarta-ant-1.5\bin\ant -buildfile build_plugin.xml %1
@call d:\jakarta-ant-1.5\bin\ant -buildfile build_ids_plugin.xml %1

