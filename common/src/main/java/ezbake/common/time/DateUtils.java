/*   Copyright (C) 2013-2014 Computer Sciences Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */

package ezbake.common.time;



import ezbake.base.thrift.Date;
import ezbake.base.thrift.DateTime;
import ezbake.base.thrift.Time;

import java.util.Calendar;

/**
 * Created by jpercivall on 7/16/14.
 */
public final class DateUtils {

    public static Time getCurrentTime() {
        Time currTime = new Time();
        Calendar calendar = Calendar.getInstance();

        // get calendar parts
        int offsetMinutes = (calendar.getTimeZone().getOffset(calendar.getTimeInMillis())) / (1000 * 60);

        currTime.setMillisecond((short) calendar.get(Calendar.MILLISECOND));
        currTime.setSecond((short) calendar.get(Calendar.SECOND));
        currTime.setMinute((short) calendar.get(Calendar.MINUTE));
        currTime.setHour((short) calendar.get(Calendar.HOUR_OF_DAY));

        boolean afterUtc = offsetMinutes > 0;
        offsetMinutes = Math.abs(offsetMinutes);
        final ezbake.base.thrift.TimeZone tz = new ezbake.base.thrift.TimeZone((short) (offsetMinutes / 60), (short) (offsetMinutes % 60), afterUtc);
        currTime.setTz(tz);

        return currTime;
    }

    public static Date getCurrentDate() {
        Date currDate = new Date();
        Calendar calendar = Calendar.getInstance();
        currDate.setDay((short) calendar.get(Calendar.DAY_OF_MONTH));
        currDate.setMonth((short) (calendar.get(Calendar.MONTH)+1));
        currDate.setYear((short) calendar.get(Calendar.YEAR));
        return currDate;
    }

    public static DateTime getCurrentDateTime() {
        DateTime currDateTime= new DateTime();
        currDateTime.setDate(getCurrentDate());
        currDateTime.setTime(getCurrentTime());
        return currDateTime;
    }
}
