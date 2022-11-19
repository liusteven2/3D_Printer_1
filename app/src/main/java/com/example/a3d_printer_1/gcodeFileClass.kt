package com.example.a3d_printer_1

class gcodeFileClass {
    var name: String? = null
    var url: String? = null

    constructor() {}
    constructor(name: String?, url: String?) {
        this.name = name
        this.url = url
    }
}