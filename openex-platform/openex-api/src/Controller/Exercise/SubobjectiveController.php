<?php

namespace App\Controller\Exercise;

use App\Controller\Base\BaseController;
use App\Entity\Exercise;
use App\Entity\Objective;
use Doctrine\Persistence\ManagerRegistry;
use FOS\RestBundle\Controller\Annotations as Rest;
use FOS\RestBundle\View\View;
use JetBrains\PhpStorm\Pure;
use OpenApi\Annotations as OA;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Security\Core\Authentication\Token\Storage\TokenStorageInterface;

class SubobjectiveController extends BaseController
{
    private ManagerRegistry $doctrine;

    public function __construct(ManagerRegistry $doctrine, TokenStorageInterface $tokenStorage)
    {
        $this->doctrine = $doctrine;
        parent::__construct($tokenStorage);
    }

    /**
     * @OA\Response(
     *    response=200,
     *    description="List subobjectives of an exercise"
     * )
     *
     * @Rest\View(serializerGroups={"subobjective"})
     * @Rest\Get("/api/exercises/{exercise_id}/subobjectives")
     */
    public function getExercisesSubobjectivesAction(Request $request)
    {
        $em = $this->doctrine->getManager();
        $exercise = $em->getRepository('App:Exercise')->find($request->get('exercise_id'));
        /* @var $exercise Exercise */

        if (empty($exercise)) {
            return $this->exerciseNotFound();
        }

        $this->denyAccessUnlessGranted('select', $exercise);

        $objectives = $em->getRepository('App:Objective')->findBy(['objective_exercise' => $exercise]);
        /* @var $objectives Objective[] */

        $subobjectives = array();
        foreach ($objectives as $objective) {
            $subobjectives = array_merge($subobjectives, $em->getRepository('App:Subobjective')->findBy(['subobjective_objective' => $objective]));
        }

        foreach ($subobjectives as &$subobjective) {
            $subobjective->setSubobjectiveExercise($exercise->getExerciseId());
            $subobjective->setUserCanUpdate($this->hasGranted(self::UPDATE, $subobjective));
            $subobjective->setUserCanDelete($this->hasGranted(self::DELETE, $subobjective));
        }
        return $subobjectives;
    }

    private function exerciseNotFound()
    {
        return View::create(['message' => 'Exercise not found'], Response::HTTP_NOT_FOUND);
    }
}
