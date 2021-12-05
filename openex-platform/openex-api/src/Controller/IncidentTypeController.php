<?php

namespace App\Controller;

use Doctrine\Persistence\ManagerRegistry;
use FOS\RestBundle\Controller\Annotations as Rest;
use OpenApi\Annotations as OA;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;

class IncidentTypeController extends AbstractController
{
    private ManagerRegistry $doctrine;

    public function __construct(ManagerRegistry $doctrine)
    {
        $this->doctrine = $doctrine;
    }

    /**
     * @OA\Response(
     *    response=200,
     *    description="List incident types"
     * )
     * @Rest\View(serializerGroups={"IncidentType"})
     * @Rest\Get("/api/incident_types")
     */
    public function getIncidentTypesAction(Request $request)
    {
        $em = $this->doctrine->getManager();
        $types = $em->getRepository('App:IncidentType')->findAll();

        return $types;
    }
}
