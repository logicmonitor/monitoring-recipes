import java.util.Calendar

public static void main(String[] args) {
    String s1 = args[0]

    s1 = s1.replaceAll(":", "")

    String year = s1.substring(0, 4)
    String month = s1.substring(4, 6)
    String day = s1.substring(6, 8)
    String hour = s1.substring(8, 10)
    String minute = s1.substring(10, 12)
    String second = s1.substring(12, 14)

    Integer Intmonth = Integer.parseInt(month, 16) - 1
    month = Integer.toHexString(Intmonth)

    //println month


    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, Integer.parseInt(year, 16));
    cal.set(Calendar.MONTH, Integer.parseInt(month, 16));
    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day, 16));
    cal.set(Calendar.HOUR, Integer.parseInt(hour, 16));
    cal.set(Calendar.MINUTE, Integer.parseInt(minute, 16));
    cal.set(Calendar.SECOND, Integer.parseInt(second, 16));
    System.out.println(cal.getTime());
}

