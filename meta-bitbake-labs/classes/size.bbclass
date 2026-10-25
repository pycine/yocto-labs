do_report_size() {
    bbwarn "Installed size: $(du -sh ${D} | cut -f1)"
}
addtask report_size after do_install before do_build
