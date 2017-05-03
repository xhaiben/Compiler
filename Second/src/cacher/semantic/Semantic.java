package cacher.semantic;

import cacher.generator.Generator;

import java.io.File;

/*
 * Created by xhaiben on 17-4-23.
 */


public class Semantic {
    public static Var_record tvar = new Var_record();
    public static Fun_record tfun = new Fun_record();
    public static Table table = Table.getInstance();
    public static Generator gener = Generator.getInstance();

    public Semantic(File out_file) {
        gener.set_out_file(out_file);
    }

}
