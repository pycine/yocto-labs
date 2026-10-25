# yocto-labs

Hands-on labs for learning the Yocto Project. Each lab lives in its own layer, so layers can be enabled independently.

| Layer | Topic | Status |
|---|---|---|
| `meta-bitbake-labs` | BitBake basics: layers, classes, recipes (Chapter 4) | done |

Tested with Poky 6.0 (`wrynose`), BitBake 2.18, `MACHINE = "qemux86-64"`.

## Repository layout

```
yocto-labs/
├── README.md
└── meta-bitbake-labs/
    ├── conf/layer.conf
    ├── classes/
    │   └── size.bbclass
    ├── recipes-hello/
    │   └── hello/hello_1.0.bb
    ├── recipes-example/
    │   └── example/example_0.1.bb
    ├── COPYING.MIT
    └── README
```

`yocto-labs` is only a container and has no `conf/layer.conf`. Each `meta-*` folder inside it is a real layer.

## Using a layer

From an initialized build directory:

```bash
bitbake-layers add-layer /path/to/yocto-labs/meta-bitbake-labs
bitbake-layers show-layers
```

Expected: a `meta-bitbake-labs` entry with priority 6.

## Lab: meta-bitbake-labs (Chapter 4)

### Goal

Inspect the metadata that BitBake loads, then create a layer, a class and a recipe, and see them work together.

### Part A: inspect the existing layers

```bash
bitbake-layers show-layers
cat <path-to>/meta-yocto-bsp/conf/layer.conf
bitbake-layers show-appends linux-yocto
bitbake-layers show-recipes "linux-yocto"
bitbake-layers show-overlayed
bitbake-getvar BBFILE_PRIORITY_yocto
bitbake-getvar -r linux-yocto PV
```

What this showed:
- `core`, `yoctobsp` and `yocto` all have priority 5. A `workspacelayer` at priority 99 also appears, created by `devtool`.
- `linux-yocto` is provided by OpenEmbedded-Core (`meta`) and modified by one `.bbappend` from `meta-yocto-bsp`.
- No overlayed files, because no two layers provide the same recipe.
- `bitbake-getvar` traces each value to the file and line that set it.

### Part B: create a layer, class and recipe

```bash
bitbake-layers create-layer ../yocto-labs/meta-bitbake-labs
bitbake-layers add-layer ../yocto-labs/meta-bitbake-labs
```

**`classes/size.bbclass`** defines a shell task and schedules it after `do_install`:

```bitbake
do_report_size() {
    bbwarn "Installed size: $(du -sh ${D} | cut -f1)"
}
addtask report_size after do_install before do_build
```

**`recipes-hello/hello/hello_1.0.bb`** installs a tiny script and inherits the class:

```bitbake
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
```

Build and verify:

```bash
bitbake hello
bitbake-layers show-recipes hello
bitbake-getvar -r hello PV
```

Expected:
- A warning like `hello-1.0-r0 do_report_size: Installed size: 4.0K`
- `hello` listed under `meta-bitbake-labs`, version 1.0
- `PV="1.0"`, taken from the recipe filename

## Lessons learned

- **Recipe folders must match `recipes-*/*/*.bb`.** A folder named just `recipes/` is never scanned, so BitBake reports that nothing provides the recipe.
- **`inherit <name>` uses the class filename** without `.bbclass`.
- **`${VAR}` is a BitBake variable and `$(cmd)` is shell command substitution.** Writing `$(D)` runs a command named `D` and gives an empty result.
- **`bbwarn` is a BitBake helper.** A typo such as `bbawrn` fails with exit code 127 (`not found`).
- **A function becomes a task only with `addtask`.** The `after` and `before` constraints decide when it runs.
- **Debugging tip:** generated scripts are in `tmp/work/*/hello/1.0*/temp/run.do_*`. The exact code BitBake ran is there.
- **Collection names are not folder names.** Dependencies use the collection name (`core`), not `meta`.

## Debugging commands

| Command | Use |
|---|---|
| `bitbake-layers show-layers` | layers, paths and priorities |
| `bitbake-layers show-appends <recipe>` | which `.bbappend` files apply |
| `bitbake-layers show-recipes <name>` | which layer provides a recipe |
| `bitbake -e <recipe>` | full merged environment with history |
| `bitbake-getvar -r <recipe> <VAR>` | one variable after the merge |

## License

See `meta-bitbake-labs/COPYING.MIT`.
