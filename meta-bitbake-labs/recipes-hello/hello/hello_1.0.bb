SUMMARY = "Lab recipe"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

S = "${UNPACKDIR}"

inherit size
do_install() {
    install -d ${D}${bindir}
    echo '#!/bin/sh' > ${D}${bindir}/hello-lab
    echo 'echo hello from meta-lab' >> ${D}${bindir}/hello-lab
    chmod 755 ${D}${bindir}/hello-lab
}