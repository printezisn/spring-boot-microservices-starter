const merge = require('webpack-merge');
const common = require('./webpack.common.js');

const path = require('path');
const glob = require('glob-all');
const webpack = require('webpack');
const UglifyJSPlugin = require('uglifyjs-webpack-plugin');
const OptimizeCssAssetsPlugin = require('optimize-css-assets-webpack-plugin');
const Critters = require('critters-webpack-plugin');
const CompressionPlugin = require('compression-webpack-plugin');

module.exports = merge(common, {
    devtool : 'source-map',
    mode : 'production',
    plugins : [
        new OptimizeCssAssetsPlugin({
            assetNameRegExp : /\.min.css$/
        }),
        new UglifyJSPlugin({
            sourceMap : true,
            extractComments : true,
            parallel : true
        }),
        new CompressionPlugin(),
        new Critters({
            preload : 'swap',
            preloadFonts : true,
            noscriptFallback : false,
            pruneSource : false,
            mergeStylesheets: false
        }),
        new webpack.DefinePlugin({
            'process.env.NODE_ENV' : JSON.stringify('production')
        })
    ]
});