/*
   Copyright (c) 2014,2015,2016 Ahome' Innovation Technologies. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.ait.lienzo.client.core.shape;

import java.util.Arrays;
import java.util.List;

import com.ait.lienzo.client.core.Attribute;
import com.ait.lienzo.client.core.Context2D;
import com.ait.lienzo.client.core.config.LienzoCore;
import com.ait.lienzo.client.core.shape.json.validators.ValidationContext;
import com.ait.lienzo.client.core.shape.json.validators.ValidationException;
import com.ait.lienzo.client.core.types.BoundingBox;
import com.ait.lienzo.client.core.types.DashArray;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.core.types.Point2DArray;
import com.ait.lienzo.shared.core.types.ShapeType;
import com.google.gwt.json.client.JSONObject;

/**
 * Line is a line segment between two points.
 * The class can be used to draw regular lines as well as dashed lines.
 * To create a dashed line, use one of the setDashArray() methods.
 */
public class Line extends AbstractOffsetMultiPointShape<Line>
{
    private double  m_tailOffsetValue = 0;

    private double  m_headOffsetValue = 0;

    private Point2D m_tailOffsetPoint = null;

    private Point2D m_headOffsetPoint = null;

    /**
     * Constructor.  Creates an instance of a line of 0-pixel length, at the 0,0
     * coordinates.
     */
    public Line()
    {
        this(0, 0, 0, 0);
    }

    /**
     * Constructor. Creates an instance of a line.
     * 
     * @param x1 first point X coordinate
     * @param y1 first point Y coordinate
     * @param x2 second point X coordinate
     * @param y2 second point Y coordinate
     */
    public Line(final double x1, final double y1, final double x2, final double y2)
    {
        this(new Point2D(x1, y1), new Point2D(x2, y2));
    }

    public Line(final Point2D p1, final Point2D p2)
    {
        super(ShapeType.LINE);

        setPoints(new Point2DArray(p1, p2));
    }

    protected Line(final JSONObject node, final ValidationContext ctx) throws ValidationException
    {
        super(ShapeType.LINE, node, ctx);

        refresh();
    }

    @Override
    public Line refresh()
    {
        if (m_tailOffsetValue != m_headOffsetValue)
        {
            return this;
        }
        return this;
    }

    @Override
    public BoundingBox getBoundingBox()
    {
        return new BoundingBox(getPoints());
    }

    /**
     * Draws this line
     * 
     * @param context
     */
    @Override
    protected boolean prepare(final Context2D context, final Attributes attr, final double alpha)
    {
        final Point2DArray list = attr.getPoints();

        if ((null != list) && (list.size() == 2))
        {
            if (attr.isDefined(Attribute.DASH_ARRAY))
            {
                if (false == LienzoCore.get().isNativeLineDashSupported())
                {
                    DashArray dash = attr.getDashArray();

                    if (dash != null)
                    {
                        double[] data = dash.getNormalizedArray();

                        if (data.length > 0)
                        {
                            if (setStrokeParams(context, attr, alpha))
                            {
                                Point2D p0 = list.get(0);

                                Point2D p1 = list.get(1);

                                context.beginPath();

                                drawDashedLine(context, p0.getX(), p0.getY(), p1.getX(), p1.getY(), data, attr.getStrokeWidth() / 2);
                            }
                            return true;
                        }
                    }
                }
            }
            context.beginPath();

            final Point2D p0 = list.get(0);

            context.moveTo(p0.getX(), p0.getY());

            final Point2D p1 = list.get(1);

            context.lineTo(p1.getX(), p1.getY());

            return true;
        }
        return false;
    }

    @Override public boolean parse(Attributes attr)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the end-points of this line.
     * 
     * @return Point2DArray
     */
    public Point2DArray getPoints()
    {
        return getAttributes().getPoints();
    }

    /**
     * Sets the end-points of this line.  
     * The points should be a 2-element {@link Point2DArray}
     * 
     * @param points
     * @return this Line
     */
    public Line setPoints(final Point2DArray points)
    {
        getAttributes().setPoints(points);

        return refresh();
    }

    @Override
    public Line setPoint2DArray(Point2DArray points)
    {
        return setPoints(points);
    }

    @Override
    public Point2DArray getPoint2DArray()
    {
        return getPoints();
    }

    @Override
    public Point2D getTailOffsetPoint()
    {
        return m_tailOffsetPoint;
    }

    @Override
    public Point2D getHeadOffsetPoint()
    {
        return m_headOffsetPoint;
    }

    /**
     * Empty implementation since we multi-purpose this class for regular and dashed lines.
     */
    @Override
    public void fill(Context2D context, Attributes attr, double alpha)
    {
    }

    /**
     * Draws a dashed line instead of a solid one for the shape.
     * 
     * @param context
     * @param x
     * @param y
     * @param x2
     * @param y2
     * @param da
     * @param state
     * @param plus
     */
    protected void drawDashedLine(Context2D context, double x, double y, double x2, double y2, double[] da, double plus)
    {
        final int dashCount = da.length;

        final double dx = (x2 - x);

        final double dy = (y2 - y);

        boolean xbig = (Math.abs(dx) > Math.abs(dy));

        double slope = (xbig) ? dy / dx : dx / dy;

        context.moveTo(x, y);

        double distRemaining = Math.sqrt(dx * dx + dy * dy) + plus;

        int dashIndex = 0;

        while (distRemaining >= 0.1)
        {
            double dashLength = Math.min(distRemaining, da[dashIndex % dashCount]);

            double step = Math.sqrt(dashLength * dashLength / (1 + slope * slope));

            if (xbig)
            {
                if (dx < 0)
                {
                    step = -step;
                }
                x += step;

                y += slope * step;
            }
            else
            {
                if (dy < 0)
                {
                    step = -step;
                }
                x += slope * step;

                y += step;
            }
            if (dashIndex % 2 == 0)
            {
                context.lineTo(x, y);
            }
            else
            {
                context.moveTo(x, y);
            }
            distRemaining -= dashLength;

            dashIndex++;
        }
    }

    @Override
    public List<Attribute> getBoundingBoxAttributes()
    {
        return getBoundingBoxAttributesComposed(Arrays.asList(Attribute.POINTS));
    }

    public static class LineFactory extends AbstractOffsetMultiPointShapeFactory<Line>
    {
        public LineFactory()
        {
            super(ShapeType.LINE);

            addAttribute(Attribute.POINTS, true);
        }

        @Override
        public Line create(final JSONObject node, final ValidationContext ctx) throws ValidationException
        {
            return new Line(node, ctx);
        }
    }
}