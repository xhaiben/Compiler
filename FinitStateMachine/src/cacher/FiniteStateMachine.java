package cacher;

/*
 * Created by xhaiben on 2017/3/7.
 */
public class FiniteStateMachine {
    private int yystate = 0; //开始时处于状态0
    private int yylastaccept = FMS.STATE_FAILURE;
    private int yyprev = FMS.STATE_FAILURE;
    private int yynstate = FMS.STATE_FAILURE;
    private boolean yyanchor = false;
    private byte yylook = Input.EOF;
    private Input input = new Input();
    private TableFMS fms = new TableFMS();
    private boolean endOfReads = false;

    public FiniteStateMachine() {
        input.ii_newFile(null);
        input.ii_advance();
        input.ii_pushback(1);
        input.ii_mark_start();
    }

    public void yylex() {
        while (true) {
            while (true) {
                if ((yylook = input.ii_lookahead(1)) != Input.EOF) {
                    yystate = fms.yy_next(yystate, yylook);
                    break;
                } else {
                    endOfReads = true;
                    if (yylastaccept != FMS.STATE_FAILURE) {
                        yystate = FMS.STATE_FAILURE;
                        break;
                    } else {
                        return;
                    }
                }
            }//内层while循环
            if (yystate != FMS.STATE_FAILURE) {
                System.out.println("Transition from state: " + yystate + " to state: " + yynstate + " on input char: " + (char) yylook);
                input.ii_advance();//越过当前字符，准备读入下个字符
                if ((yyanchor = fms.isAcceptState(yynstate))) {
                    yyprev = yystate;
                    yylastaccept = yynstate;
                    input.ii_mark_end();//遇到接受状态，对当前输入的字符串做标记
                }
                yystate = yynstate;
            } else {
                if (yylastaccept == FMS.STATE_FAILURE) {
                    if (yylook != '\n') {
                        System.out.println("Ignoring bad input");
                    }
                    input.ii_advance();//忽略导致状态机出错的字符
                } else {
                    input.ii_to_mark();//准备获取接受状态的字符形成的字符串
                    System.out.println("Accepting state: " + yylastaccept);
                    System.out.println("line: " + input.ii_lineno() + "accept text: " + input.ii_text());
                    switch (yylastaccept) {
                        case 1:
                            System.out.println("it is an integer");
                            break;
                        case 2:
                        case 4:
                            System.out.println(" it is a float number");
                            break;
                        default:
                            System.out.println("internal errow");
                            break;
                    }

                }
                //识别到错误字符或给出判断后，将状态机重置
                yylastaccept = FMS.STATE_FAILURE;
                yystate = 0;
                input.ii_mark_start();
            }
            if (endOfReads) {
                return; //结束外层循环
            }
        }//外层循环
    }

    public static void main(String[] args) {
        FiniteStateMachine fms = new FiniteStateMachine();
        fms.yylex();
    }
}
