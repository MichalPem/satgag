package cz.mipemco.satgag.jpa;

import cz.mipemco.satgag.dto.Meme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author Michal Pemčák
 */
public interface ImageRepository extends JpaRepository<Meme,Long>
{
}
