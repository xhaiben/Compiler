package cacher.semantic;

import cacher.generator.generator;

import java.io.File;

/*
 * Created by xhaiben on 17-4-23.
 */


public class semantic {
    public static var_record tvar = new var_record();
    public static fun_record tfun = new fun_record();
    public static Table table = Table.getInstance();
    public static generator gener = generator.getInstance();

    public semantic(File out_file) {
        gener.set_out_file(out_file);
    }

}
