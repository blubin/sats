/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.smrvm;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Preconditions;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Bundle;
import org.spectrumauctions.sats.core.model.World;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

import static org.spectrumauctions.sats.core.model.mrvm.MRVMRegionsMap.*;

/**
 * @author Michael Weiss
 *
 */
public final class SMRVMWorld extends World {

    private static final int BIGDECIMAL_PRECISON = 10;
    
    private static final long serialVersionUID = 2189142937399997527L;
    
    public static final String MODEL_NAME = "Multi-Region Value Model";
    private final SMRVMRegionsMap regionsMap;
    private final Set<SMRVMBand> bands;
    
    private transient BigDecimal maximalRegionalCapacity = null;


    public SMRVMWorld(SMRVMWorldSetup worldSetup, RNGSupplier rngSupplier) {
        super(MODEL_NAME);
        regionsMap = new SMRVMRegionsMap(worldSetup, rngSupplier);
        bands = Collections.unmodifiableSet(SMRVMBand.createBands(this, worldSetup, regionsMap, rngSupplier.getUniformDistributionRNG()));
        store();
    }


    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.model.World#getNumberOfGoods()
     */
    @Override
    public int getNumberOfGoods() {
        int numberOfLicenses = 0;
        for(SMRVMBand band : bands){
            int numberOfRegions = regionsMap.getRegions().size();
            int numberOfLots = band.getNumberOfLots();
            numberOfLicenses += numberOfLots*numberOfRegions;
        }
        return numberOfLicenses;
    }

    public Set<SMRVMBand> getBands(){
        return Collections.unmodifiableSet(bands);
    }
    
    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.model.World#getLicenses()
     */
    @Override
    public Set<SMRVMLicense> getLicenses() {
        Set<SMRVMLicense> licenses = new HashSet<>();
        for(SMRVMBand band : bands){
            licenses.addAll(band.getLicenses());
        }
        return licenses;
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.model.World#restorePopulation(long)
     */
    @Override
    public Collection<? extends Bidder<SMRVMLicense>> restorePopulation(long populationId) {
        return super.restorePopulation(SMRVMBidder.class, populationId);
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.model.World#refreshFieldBackReferences()
     */
    @Override
    public void refreshFieldBackReferences() {
        for(SMRVMBand band : bands){
            band.refreshFieldBackReferences(this);
        }
    }


    /**
     * @return
     */
    public SMRVMRegionsMap getRegionsMap() {
        return regionsMap;
    }

    
    /**
     * Sorts the licenses of a bundle into subbundles by their band.
     * The returned map contains all bands of the world as keys, even such which are not present with any licenses in the bundle.<br>
     * @param bundle Must be nonempty
     * @return
     */
    public static Map<SMRVMBand, Bundle<SMRVMLicense>> getLicensesPerBand(Bundle<SMRVMLicense> bundle){
        Preconditions.checkArgument(!bundle.isEmpty());
        SMRVMWorld world = bundle.iterator().next().getWorld();
        Map<SMRVMBand, Bundle<SMRVMLicense>> licensesPerBand = new HashMap<>();
        for(SMRVMBand band : world.getBands()){
            licensesPerBand.put(band, new Bundle<SMRVMLicense>());
        }
        for(SMRVMLicense license : bundle ){
            licensesPerBand.get(license.getBand()).add(license);
        }
        return licensesPerBand;
    }
    
    
    /**
     * Counts the number of licenses for each band.
     * The returned map contains all bands of the world as keys, even such which are not present with any licenses in the bundle.<br>
     * @param bundle Must be nonempty
     * @return
     */
    public static Map<SMRVMBand, Integer> quantitiesPerBand(Bundle<SMRVMLicense> bundle){
        Preconditions.checkArgument(!bundle.isEmpty());
        Map<SMRVMBand, Bundle<SMRVMLicense>> licensesPerBand = getLicensesPerBand(bundle);
        Map<SMRVMBand, Integer> quantities = new HashMap<>();
        for(Entry<SMRVMBand, Bundle<SMRVMLicense>> bandBundles : licensesPerBand.entrySet()){
            quantities.put(bandBundles.getKey(), bandBundles.getValue().size());
        }
        return quantities;
    }
    
    public static BigDecimal cap(Region r, Bundle<SMRVMLicense> bundle){
        if(bundle.isEmpty()){
            return BigDecimal.ZERO;
        }
        Bundle<SMRVMLicense> regionalSubBundle = getLicensesPerRegion(bundle).get(r);
        Map<SMRVMBand, Integer> bandQuantities = quantitiesPerBand(regionalSubBundle);
        BigDecimal cap = BigDecimal.ZERO;
        for(Entry<SMRVMBand, Integer> bandQuantityEntry : bandQuantities.entrySet()){
            if(bandQuantityEntry.getValue() != 0){
                BigDecimal bandCap = capOfBand(bandQuantityEntry.getKey(), bandQuantityEntry.getValue());
                cap = cap.add(bandCap);
            }       
        }
        return cap;
    }
    
    /**
     * Returns the capacity for having <i>numberOfLicenses</i> many {@link SMRVMLicense} in {@link SMRVMBand} <i>band</i>
     * @param band
     * @param numberOfLicenses
     * @return
     */
    public static BigDecimal capOfBand(SMRVMBand band, int numberOfLicenses){
        if(numberOfLicenses == 0){
            return BigDecimal.ZERO;
        }
        Preconditions.checkArgument(numberOfLicenses >= 0);
        Preconditions.checkArgument(numberOfLicenses <= band.getNumberOfLots());
        BigDecimal quantity = new BigDecimal(numberOfLicenses);
        BigDecimal baseCapacity = band.getBaseCapacity();
        BigDecimal synergy = band.getSynergy(numberOfLicenses);
        BigDecimal bandCap = quantity.multiply(baseCapacity).multiply(synergy);
        return bandCap;
    }
    
    /**
     * Sorts the licenses of a bundle into subbundles by their region.<br>
     * The returned map contains all regions of the world as keys, even such which are not present with any licenses in the bundle.<br>
     * @param bundle 
     * @return
     */
    public static Map<Region, Bundle<SMRVMLicense>> getLicensesPerRegion(Bundle<SMRVMLicense> bundle){
        Preconditions.checkArgument(!bundle.isEmpty());
        SMRVMWorld world = bundle.iterator().next().getWorld();
        Map<Region, Bundle<SMRVMLicense>> licensesPerRegion = new HashMap<>();
        for(Region region : world.getRegionsMap().getRegions()){
            licensesPerRegion.put(region, new Bundle<SMRVMLicense>());
        }
        for(SMRVMLicense license : bundle ){
            licensesPerRegion.get(license.getRegion()).add(license);
        }
        return licensesPerRegion;
    }
    
    
    public static BigDecimal capShare(Region r, Bundle<SMRVMLicense> bundle){
        if(bundle.isEmpty()){
            return BigDecimal.ZERO;
        }
        SMRVMWorld world = bundle.iterator().next().getWorld();
        BigDecimal maxCap = world.getMaximumRegionalCapacity();
        BigDecimal cap = cap(r, bundle);
        return cap.divide(maxCap, new MathContext(BIGDECIMAL_PRECISON));
    }
    
    
    public List<SMRVMBidder> createPopulation(SMRVMLocalBidderSetup localSetup,
                                              SMRVMRegionalBidderSetup regionalSetup,
                                              SMRVMGlobalBidderSetup globalSetup,
                                              RNGSupplier rngSupplier){
       Collection<SMRVMLocalBidderSetup> localSetups = null;
       Collection<SMRVMRegionalBidderSetup> regionalSetups = null;
       Collection<SMRVMGlobalBidderSetup> globalSetups = null;
       if(localSetup != null){
           localSetups = new HashSet<>();
           localSetups.add(localSetup);
       }
       if(regionalSetup != null){
           regionalSetups = new HashSet<>();
           regionalSetups.add(regionalSetup);
       }
       if(globalSetup != null){
           globalSetups = new HashSet<>();
           globalSetups.add(globalSetup);
       }
       return createPopulation(localSetups, regionalSetups, globalSetups, rngSupplier);        
    }
        
        
    public List<SMRVMBidder> createPopulation(Collection<SMRVMLocalBidderSetup> localSetups,
                                              Collection<SMRVMRegionalBidderSetup> regionalSetups,
                                              Collection<SMRVMGlobalBidderSetup> globalSetups,
                                              RNGSupplier rngSupplier){
        long population = openNewPopulation();
        List<SMRVMBidder> bidders = new ArrayList<>();
        int idCount = 0;
        if(localSetups != null){
            for(SMRVMLocalBidderSetup setup : localSetups){
                for(int i = 0; i < setup.getNumberOfBidders(); i++){
                    bidders.add(new SMRVMLocalBidder(idCount++, population, this, setup, rngSupplier.getUniformDistributionRNG()));
                }
            }
        }
        if(regionalSetups != null){
            for(SMRVMRegionalBidderSetup setup : regionalSetups){
                for(int i = 0; i < setup.getNumberOfBidders(); i++){
                    bidders.add(new SMRVMRegionalBidder(idCount++, population, this, setup, rngSupplier.getUniformDistributionRNG()));
                }
            }
        }
        if(globalSetups != null){
            for(SMRVMGlobalBidderSetup setup : globalSetups){
                for(int i = 0; i < setup.getNumberOfBidders(); i++){
                    bidders.add(new SMRVMGlobalBidder(idCount++, population, this, setup, rngSupplier.getUniformDistributionRNG()));
                }
            }
        }
        Preconditions.checkArgument(bidders.size() > 0, "At least one bidder setup with a strictly positive number of bidders is required to generate population");
        return bidders;
    }

    /**
     * Calculates the maximum capacity any region can have.
     * The result is cashed, hence, calling the method multiple time is not costly.
     * @return
     */
    public BigDecimal getMaximumRegionalCapacity() {
        if(maximalRegionalCapacity == null){
            Region anyRegion = regionsMap.getRegions().iterator().next();
            maximalRegionalCapacity = cap(anyRegion, new Bundle<>(getLicenses()));
        }
        return maximalRegionalCapacity;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((bands == null) ? 0 : bands.hashCode());
        result = prime * result + ((regionsMap == null) ? 0 : regionsMap.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SMRVMWorld other = (SMRVMWorld) obj;
        if (bands == null) {
            if (other.bands != null)
                return false;
        } else if (!bands.equals(other.bands))
            return false;
        if (regionsMap == null) {
            if (other.regionsMap != null)
                return false;
        } else if (!regionsMap.equals(other.regionsMap))
            return false;
        return true;
    }
    

}
