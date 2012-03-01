/*
 * Copyright 2004 - 2011 Brian McCallister
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.skife.jdbi.v2;

import org.skife.jdbi.v2.tweak.Argument;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 *
 */
class ObjectArgument implements Argument
{
    private final Object value;

    ObjectArgument(Object value)
    {
        this.value = value;
    }

    public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException
    {
        if (value == null) {
            statement.setNull(position, Types.OTHER);
        }
        else if (value.getClass().isEnum()) {
            // In many cases object is the fallback, and we want to default to binding enums
            // as strings
            statement.setString(position, value.toString());
        }
        else {
            statement.setObject(position, value);
        }
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
