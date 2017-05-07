/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.smrvm;

import org.spectrumauctions.sats.core.model.BidderSetup;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.UniformDistributionRNG;

import java.math.BigDecimal;
/**
 * @author Michael Weiss
 *
 */
public abstract class SMRVMBidderSetup extends BidderSetup {
    
    private DoubleInterval alphaInterval;
    private DoubleInterval betaInterval;


    /**
     * @param alphaInterval
     * @param betaInterval
     */
    protected SMRVMBidderSetup(Builder builder) {
        super(builder);
        this.alphaInterval = builder.alphaInterval;
        this.betaInterval = builder.betaInterval;
    }

    /**
     * Draws the Alpha-Parameter uniformly at random.<br>
     * Alpha is a parameter defining an expected profit per served customer, if quality of service and regional discount are ignored.<br>
     * It can be understood as a relative bidder strength parameter.
     * @param rng
     * @return a BigDecimal in [0,1]
     */
    public BigDecimal drawAlpha(UniformDistributionRNG rng) {
        return rng.nextBigDecimal(alphaInterval);
    }

    /**
     * Draws the Beta-Parameter uniformly at random.<br> 
     * Beta is a parameter defining the target market share this bidder intends to cover. <br>
     * The bidders value for a bundle increases heavily as soon as the capacity share he has in a region gets close to beta.
     * @param rng
     * @return a BigDecimal in [0,1]
     */
    public BigDecimal drawBeta(UniformDistributionRNG rng) {
        return rng.nextBigDecimal(betaInterval);
    }
    
    
    public static abstract class Builder extends BidderSetup.Builder{
        
        private DoubleInterval alphaInterval;
        private DoubleInterval betaInterval;
        
  
        /**
         * @param alphaInterval
         * @param betaInterval
         */
        protected Builder(String setupName, int numberOfBidders, DoubleInterval alphaInterval, DoubleInterval betaInterval) {
            super(setupName, numberOfBidders);
            this.alphaInterval = alphaInterval;
            this.betaInterval = betaInterval;
        }
        
        
        
        /**
         * The interval from which the alpha value will be drawn. <br>
         * See {@link SMRVMBidderSetup#alphaInterval} for explanation of alpha-parameter
         * @return
         */
        public DoubleInterval getAlphaInterval() {
            return alphaInterval;
        }



        /**
         * The interval from which the alpha value will be drawn. <br>
         * See {@link SMRVMBidderSetup#alphaInterval} for explanation of alpha-parameter
         * @return
         */
        public void setAlphaInterval(DoubleInterval alphaInterval) {
            this.alphaInterval = alphaInterval;
        }



        /**
         * The interval from which the beta value will be drawn. <br>
         * See {@link SMRVMBidderSetup#betaInterval} for explanation of beta-parameter
         * @return
         */
        public DoubleInterval getBetaInterval() {
            return betaInterval;
        }



        /**
         * The interval from which the beta value will be drawn. <br>
         * See {@link SMRVMBidderSetup#betaInterval} for explanation of beta-parameter
         * @return
         */
        public void setBetaInterval(DoubleInterval betaInterval) {
            this.betaInterval = betaInterval;
        }




        @Override
        public abstract SMRVMBidderSetup build();
    }

}
